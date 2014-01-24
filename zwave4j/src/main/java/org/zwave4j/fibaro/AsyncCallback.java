package org.zwave4j.fibaro;

/** Invoked with the result (or error) of an RPC. */
public interface AsyncCallback<T> {
  /**
   * Called when an asynchronous call fails to complete normally.
   * {@link com.google.gwt.user.client.rpc.InvocationException}s,
   * or checked exceptions thrown by the service method are examples of the type
   * of failures that can be passed to this method.
   *
   * @param caught failure encountered while executing a remote procedure call
   */
  void onFailure(Throwable caught);

  /**
   * Called when an asynchronous call completes successfully.
   *
   * @param result the return value of the remote produced call
   */
  void onSuccess(T result);
}