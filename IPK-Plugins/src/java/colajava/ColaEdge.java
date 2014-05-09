/*
 * ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.31
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * -----------------------------------------------------------------------------
 */

package colajava;

public class ColaEdge {
	private long swigCPtr;
	protected boolean swigCMemOwn;
	
	protected ColaEdge(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
	}
	
	protected static long getCPtr(ColaEdge obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}
	
	@Override
	protected void finalize() {
		delete();
	}
	
	public synchronized void delete() {
		if (swigCPtr != 0 && swigCMemOwn) {
			swigCMemOwn = false;
			colaJNI.delete_ColaEdge(swigCPtr);
		}
		swigCPtr = 0;
	}
	
	public ColaEdge() {
		this(colaJNI.new_ColaEdge__SWIG_0(), true);
	}
	
	public ColaEdge(long first, long second) {
		this(colaJNI.new_ColaEdge__SWIG_1(first, second), true);
	}
	
	public ColaEdge(ColaEdge p) {
		this(colaJNI.new_ColaEdge__SWIG_2(ColaEdge.getCPtr(p), p), true);
	}
	
	public void setFirst(long value) {
		colaJNI.ColaEdge_first_set(swigCPtr, this, value);
	}
	
	public long getFirst() {
		return colaJNI.ColaEdge_first_get(swigCPtr, this);
	}
	
	public void setSecond(long value) {
		colaJNI.ColaEdge_second_set(swigCPtr, this, value);
	}
	
	public long getSecond() {
		return colaJNI.ColaEdge_second_get(swigCPtr, this);
	}
	
}
