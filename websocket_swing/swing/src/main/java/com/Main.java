package com;

import com.poc.model.EventEmitter;
import com.poc.model.PocModel;
import com.poc.presentation.PocPresenter;
import com.poc.presentation.PocView;

import java.util.concurrent.CountDownLatch;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        var latch = new CountDownLatch(1);
		var pocView = new PocView();
		var eventEmitter = new EventEmitter();
		var pocModel = new PocModel(eventEmitter);
		var _ = new PocPresenter(pocView, pocModel, eventEmitter);

		latch.await();

	}
}
