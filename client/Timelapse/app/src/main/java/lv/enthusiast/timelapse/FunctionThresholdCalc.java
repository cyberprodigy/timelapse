package lv.enthusiast.timelapse;

/**
 * Created by mitnick on 4/22/14.
 */
public class FunctionThresholdCalc {
    private int _currentElem;
    private float _maxValue;
    private int _numOfElems;
    private float _stepSize;
    private float _lastSentValue;

    public void init(float maxValue, int numberOfElems, float stepSize) {
        _currentElem = 0;
        _maxValue = maxValue;
        _numOfElems = numberOfElems;
        _stepSize = stepSize;
    }

    public float getNextElem() {
        float perfectValue = (_maxValue/_numOfElems * _currentElem);
        float reminder = perfectValue%_stepSize;
        float thresholdValue;
        if(_currentElem >= _numOfElems) {
            return -1;
        }
        if(reminder>=(_stepSize/2)) {
            thresholdValue = (Math.round(perfectValue/_stepSize)+1) * _stepSize;
        }
        else {
            thresholdValue = Math.round(perfectValue/_stepSize) * _stepSize;
        }
        _currentElem++;
        if(_lastSentValue != thresholdValue) {
            _lastSentValue = thresholdValue;
            return _stepSize;
        }
        else {
            return 0;
        }
    }
}
