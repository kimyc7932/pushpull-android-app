package com.mm.android.mobilecommon.pps.provider;

/**
 * 특정 비즈니스 로직에 국한되지 않는 범용 인터페이스
 * 리턴타입이 보이드이고  argument가 없다는 점이 java.util.concurrent.Callable 과 다른 점이다.
 * 리턴타입이 존재하지 않는 다는 점에서는 Runnable과 흡사하나 argument가 존재하고 
 * Thread 와 개념적 연관성이 없다는 것이 차이점이다. 
 * @author tooktok
 *
 * @param <Param>
 */
public interface Processable<Param> {

	public void process(Param param);
}
