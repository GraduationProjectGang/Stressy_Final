package com.android.stressy.etc

import android.os.Environment
import org.datavec.api.records.reader.RecordReader
import org.datavec.api.records.reader.impl.csv.CSVRecordReader
import org.datavec.api.split.FileSplit
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.LSTM
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration
import org.deeplearning4j.nn.transferlearning.TransferLearning
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.learning.config.Nesterovs
import org.nd4j.linalg.learning.config.Sgd
import org.nd4j.linalg.lossfunctions.LossFunctions
import java.io.File

object TrainModel {
    val dir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val syncDir =
        Environment.getExternalStoragePublicDirectory("DropsyncFiles")
    val locateToSaveDataSet = File("raw/labelled_dataset.csv")
    val locateToLoadModel = File("raw/trained_har_nn.zip")
    var id: String? = null
    private const val numHiddenNodes = 1000
    private const val numOutputs = 6
    private const val nEpochs = 10
    var isTransferred = false

    //for lstm
    const val LEARNING_RATE = 0.05
    const val lstmLayerSize = 300
    const val NB_INPUTS = 86
    var fineTuneConf = FineTuneConfiguration.Builder()
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .updater(Nesterovs(5e-5))
        .seed(100)
        .build()
    var model: MultiLayerNetwork? = null
    fun TrainingModel(file: File?): MultiLayerNetwork? {
        var transferred_model = model
        if (!isTransferred) {
            transferred_model = TransferLearning.Builder(model)
                .fineTuneConfiguration(fineTuneConf)
                .setFeatureExtractor(1)
                .build()
            isTransferred = true
        }
        val numLabelClasses = 4
        val conf = NeuralNetConfiguration.Builder()
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(Sgd(LEARNING_RATE))
            .graphBuilder()
            .addInputs("trainFeatures")
            .setOutputs("predictMortality")
            .addLayer(
                "L1", LSTM.Builder()
                    .nIn(NB_INPUTS)
                    .nOut(lstmLayerSize)
                    .activation(Activation.SOFTSIGN)
                    .weightInit(WeightInit.DISTRIBUTION)
                    .build(), "trainFeatures"
            )
            .addLayer(
                "predictMortality",
                RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                    .activation(Activation.SOFTMAX)
                    .weightInit(WeightInit.DISTRIBUTION)
                    .nIn(lstmLayerSize).nOut(numLabelClasses).build(),
                "L1"
            ) //                .pretrain(false).backprop(true)
            .build()
        val rr: RecordReader = CSVRecordReader()
        try {
            rr.initialize(FileSplit(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val trainIter: DataSetIterator = RecordReaderDataSetIterator(rr, 10, 0, 6)
        transferred_model!!.fit(trainIter, nEpochs)
        return transferred_model
    }
}