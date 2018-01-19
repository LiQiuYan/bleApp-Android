package com.healthme.common.nio.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthme.common.nio.Message;

public class DispatcherEventHandler {
	private static Logger log = Logger.getLogger(DispatcherEventHandler.class);

	private final WorkerPool workerPool;
	private int bufferSize;

	DispatcherEventHandler(WorkerPool workerPool, int bufferSize) {
		this.workerPool = workerPool;
		this.bufferSize = bufferSize;
	}

	public WorkerPool getWorkerPool() { return workerPool; }

	public int getBufferSize() { return bufferSize; }
	public void setBufferSize(int value) { bufferSize = value; }

	public void onHandleReadableEvent(final ChannelInfo cinfo) throws IOException {			
		cinfo.resetTimer();
		try {
			ChannelInfo myChannel = null;
			// read data from socket
			if (cinfo.getChannelType() == ChannelInfo.TCP_CHANNEL && readSocketIntoQueue(cinfo) < 0){
				// channel is closed
				cinfo.getDispatcher().deregister(cinfo);
				return;
			}

			final ChannelInfo theChannel = myChannel == null ? cinfo : myChannel;
			if (!theChannel.hasReadableData()) {
				return;
			}

			// handle it, if there is data for app handler to process
			// synchronously read data from the channel
			workerPool.execute(new Runnable() {
				public void run() {
					synchronized(theChannel) {
						try {
							ByteBuffer buf = null;
							switch (theChannel.getRecvMode()) {
							case NetService.RECVMODE_CHECK_BOUNDARY:
//								boolean isReturnQueueTimeout = theChannel.isReturnQueueTimeout();
//								boolean isReturnQueueEmpty = theChannel.isReturnQueueEmpty();
								if ((buf = theChannel.receiveAll()) == null) {
									return;
								}

							    List<Message> bufList = theChannel.onCheck(buf);
//								if (buf.remaining() > 0) {
//									if (isReturnQueueTimeout) {
//										// leave application to handle timeout data in the return queue
//										log.warn("Drop timeout buffer:"+new String(buf.array()));
//									} else {
//										log.debug("incomplete message received, return to the queue");
//										theChannel.returnReceive(buf);
//										if (isReturnQueueEmpty || bufList != null && !bufList.isEmpty()) {
//											theChannel.resetReturnQueueTime();
//										}
//										theChannel.getDispatcher().updateInterestSet(theChannel, SelectionKey.OP_READ);
//									}
//								}
								if (bufList == null || bufList.isEmpty()) {
									return;
								}

								if (bufList.size() > 1) {
									if( log.isDebugEnabled() )
										log.debug("split the received data into " + bufList.size() + " messages");
								}

								for (int i = 0; i < bufList.size(); i++) {
									doReadDataByThread(cinfo, theChannel, bufList.get(i));	
								}
								// doReadData(cinfo, theChannel, bufList.get(0));	// within current thread

								break;

							default:	// RECVMODE_RECEIVE_ONE_BY_ONE
								log.warn("Unsupported receiving type!");
								break;
							}

						} catch (Exception ex) {
							log.error("[" + theChannel + "] error occured by handling data: " + ex.getMessage(), ex);
						}
					}
				}
			});			

		} catch (Throwable t) {
			cinfo.close();
			log.error("[" + cinfo + "] error occured by handling readable event: " + t.getMessage(), t);
		}
	}

	private void doReadDataByThread(final ChannelInfo cinfo, final ChannelInfo theChannel, final Message buf) {
		if (buf == null ) {
			return;
		}
		workerPool.execute(new Runnable() {
			public void run() {
				try {
					handleData(cinfo, theChannel, buf);
				} catch (Exception ex) {
					log.error("[" + theChannel + "] error occured by handling data: " + ex.getMessage(), ex);
				}
			}
		});
	}

	private void handleData(final ChannelInfo cinfo, ChannelInfo theChannel, Message msg) throws Exception {
		if (msg == null) {
			return;
		}
		if( log.isDebugEnabled() )
			log.debug(Thread.currentThread().getName() + " processes onData() event");
		if (theChannel.getChannelType() == ChannelInfo.TCP_CHANNEL) {
			theChannel.getAppHandler().onData(theChannel, msg);

			if (theChannel.hasWriteableData() && theChannel.isConnected()) {
				theChannel.getDispatcher().announceWriteNeed(theChannel);
			}
		}									
	}

	public void onHandleWriteableEvent(final ChannelInfo cinfo) throws IOException {

		cinfo.getDispatcher().updateInterestSet(cinfo, SelectionKey.OP_READ);
		cinfo.resetTimer();

		// write data to socket 
		try {
			writeSendQueueDataToSocket(cinfo);

			if (cinfo.getChannelType() == ChannelInfo.TCP_CHANNEL && !cinfo.getChannel().isConnected()) {
				cinfo.getDispatcher().deregister(cinfo);

			} else if (cinfo.hasWriteableData()) {
				// there is remaining data to write
				cinfo.getDispatcher().announceWriteNeed(cinfo);
			}

		} catch(IOException ioe)  {
			cinfo.close();
			log.error("error occured by handling writable event: " + ioe.getMessage(), ioe);
		}
	}

	public void onHandleConnectEvent(final ChannelInfo cinfo) throws IOException {
		final IAppHandler appHandler = cinfo.getAppHandler();
		cinfo.resetTimer();

		if (appHandler instanceof IAppHandlerConnect) {
			// call connect event
			workerPool.execute(new Runnable() {
				public void run() {
					if( log.isDebugEnabled() )
						log.debug(Thread.currentThread().getName() + " processes onHandleConnectEvent(), open connection to " + cinfo);
					try {
						((IAppHandlerConnect)appHandler).onConnect(cinfo);
					} catch (Exception ex) {
						log.error("[" + cinfo + "] error occured by onConnect: " + ex.getMessage(), ex);
					}
				}
			});
		}	
	}

	public void onHandleDisconnectEvent(final ChannelInfo cinfo) {
		final IAppHandler appHandler = cinfo.getAppHandler();
		cinfo.resetTimer();

		if (appHandler instanceof IAppHandlerConnect) {
			// call disconnect event
			workerPool.execute(new Runnable() {
				public void run() {
					if( log.isDebugEnabled() )
						log.debug(Thread.currentThread().getName() + " processes onDispatcherCloseEvent()");	
					try {
						((IAppHandlerConnect)appHandler).onDisconnect(cinfo);

						// clear the message fragment in the buffer
						cinfo.clearReturnQueue();
						// after process, close this channel and drop from selector
						closeSocket(cinfo);

					} catch (Exception ex) {
						log.error("[" + cinfo + "] error occured by disconnecting: " + ex.getMessage(), ex);
					}
				}
			});

		} else {
			closeSocket(cinfo);
		}
	}

	public void onHandleTimeoutEvent(final ChannelInfo cinfo) {
		final IAppHandler appHandler = cinfo.getAppHandler();
		cinfo.resetTimer();

		if (appHandler instanceof IAppHandlerTimeout) {
			workerPool.execute(new Runnable() {
				public void run() {
					if( log.isDebugEnabled() )
						log.debug(Thread.currentThread().getName() + " processes onHandleTimeoutEvent()");
					try {
						boolean result = ((IAppHandlerTimeout)appHandler).onTimeout(cinfo);

						if (result) {
							log.info("timeout: " + cinfo);
							cinfo.getDispatcher().deregister(cinfo);
						}
					} catch (Exception ex) {
						log.error("[" + cinfo + "] error occured by timeout: " + ex.getMessage(), ex);
					}

				}
			});

		} else {
			log.info("timeout: " + cinfo);
			try {
				cinfo.getDispatcher().deregister(cinfo);
			} catch (Exception ignore) {
			}
		}
	}

	public void onHandleAcceptableEvent(final ChannelInfo cinfo) {
		cinfo.resetTimer();
		try {
			SocketChannel channel = cinfo.getServerChannel().accept();

			// configure the channel
			channel.configureBlocking(false);

			IAppHandler myAppHandler = null;
			if (!cinfo.getAppHandlerScoped()) {
				myAppHandler = cinfo.getAppHandler();
			} else {
				myAppHandler = (IAppHandler)cinfo.getAppHandler().getClass().newInstance();
			}

			// create IoSocketHandler
			Dispatcher dispatcher = cinfo.getDispatcher().getDispatcherPool().nextDispatcher();
			ChannelInfo clientConn = new ChannelInfo(channel, myAppHandler, dispatcher);
			clientConn.setTimeoutSec(cinfo.getTimeoutSec());
			clientConn.setRecvMode(cinfo.getRecvMode());

			if( log.isDebugEnabled() )
				log.debug("Accept a connection from " + channel.socket().getRemoteSocketAddress());
			dispatcher.register(clientConn, SelectionKey.OP_READ);

		} catch (Throwable t) {
			cinfo.close();
			log.error("error occured by handling readable event: " + t.getMessage(), t);
		}
	}

	/**
	 * reads socket into read queue for TCP_CHANNEL
	 * 
	 * @return the number of read bytes, -1 = channel error (closed by peer)
	 * @throws IOException If some other I/O error occurs
	 */
	protected final int readSocketIntoQueue(ChannelInfo cinfo) throws IOException {
		ByteBuffer readBuffer = null;
		int readLen = 0, l;
		List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();

		do {
			readBuffer = ByteBuffer.allocate(bufferSize);
			try {
				l = cinfo.getChannel().read(readBuffer);	// invalid channel: l=-1 or IOException
			} catch (IOException e) {
				if (cinfo.getChannelType() == ChannelInfo.TCP_CHANNEL && (!cinfo.getChannel().isOpen() || 
						e.getMessage().startsWith("Connection reset"))) {
					// Connection reset by peer
					l = -1;
				} else {
					throw e;
				}
			}
			if (l < 0) {	// EOF
				if (l < 0 && readLen == 0) {
					readLen = -1;
				}
				break;
			} else if (l == 0) {
				break;
			}

			readLen += l;
			readBuffer.position(0);
			readBuffer.limit(l);
			buffers.add(readBuffer.slice());
		} while (l > 0);

		if (readLen <= 0) {
			return readLen;
		}
		if( log.isDebugEnabled() )
			log.debug("[" + cinfo + "] received " + readLen + " bytes");
		cinfo.putIntoReceiveQueue(buffers.toArray(new ByteBuffer[buffers.size()]));

		return readLen;
	}

	/**
	 * writes the content of the send queue to the socket
	 * 
	 * @throws IOException If some other I/O error occurs
	 * @throws ClosedConnectionException if the underlying channel is closed  
	 */
	protected final long writeSendQueueDataToSocket(ChannelInfo cinfo) throws IOException {
		long writeLen = 0;
		if (cinfo.hasWriteableData()) {

			ByteBuffer[] data = cinfo.drainSendQueue();

			if (cinfo.getChannelType() == ChannelInfo.TCP_CHANNEL) {
				writeLen = cinfo.getChannel().write(data);
				if( log.isDebugEnabled() )
					log.debug("[" + cinfo + "] sending " + writeLen + " bytes");
			}

			// network too slow: size to send was more than the socket buffer output size accepts.
			// put back all unsent data in the buffer array into send queue
			cinfo.putIntoSendQueue(data);

		}
		return writeLen;
	}

	/**
	 * Physically close socket, automatically drop from selector registered
	 * no more event triggered.
	 * 
	 * @param cinfo
	 */
	protected final void closeSocket(ChannelInfo cinfo) {
		log.info("close " + cinfo);
		try {
			if (cinfo.getChannelType() == ChannelInfo.TCP_CHANNEL) {
				cinfo.getChannel().close();
			}
			cinfo.getSelectableChannel().close();
		} catch (Exception ignore) {
			// ignore
		}
	}
}
