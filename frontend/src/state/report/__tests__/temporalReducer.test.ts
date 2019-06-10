import {makeThreshold} from '../../../__tests__/testDataFactory';
import {Period, TemporalResolution} from '../../../components/dates/dateModels';
import {selectSavedSelectionAction, setThreshold} from '../../user-selection/userSelectionActions';
import {SelectionInterval, ThresholdQuery, UserSelection} from '../../user-selection/userSelectionModels';
import {initialState as userSelectionState} from '../../user-selection/userSelectionReducer';
import {selectResolution, setReportTimePeriod, toggleComparePeriod} from '../reportActions';
import {ReportSector, TemporalReportState} from '../reportModels';
import {initialState, temporalReducerFor} from '../temporalReducer';

describe('temporal', () => {

  const temporal = temporalReducerFor(ReportSector.report);

  describe('change period', () => {

    it('can change its time period', () => {
      const timePeriod: SelectionInterval = {period: Period.currentMonth};

      const state: TemporalReportState = temporal(initialState, setReportTimePeriod(ReportSector.report)(timePeriod));

      const expected: TemporalReportState = {...initialState, timePeriod, resolution: TemporalResolution.day};
      expect(state).toEqual(expected);
    });
  });

  describe('selectResolution', () => {

    it('can select hourly resolution', () => {
      const payload = TemporalResolution.hour;

      const state: TemporalReportState = temporal(initialState, selectResolution(ReportSector.report)(payload));

      const expected: TemporalReportState = {...initialState, resolution: payload};
      expect(state).toEqual(expected);
    });

    it('changes resolution', () => {
      const payload = TemporalResolution.hour;

      let state: TemporalReportState = temporal(initialState, selectResolution(ReportSector.report)(payload));

      let expected: TemporalReportState = {...initialState, resolution: payload};
      expect(state).toEqual(expected);

      state = temporal(initialState, selectResolution(ReportSector.report)(TemporalResolution.month));

      expected = {...initialState, resolution: TemporalResolution.month};
      expect(state).toEqual(expected);
    });

    it('can select all as resolution', () => {
      const payload = TemporalResolution.all;

      const state: TemporalReportState = temporal(initialState, selectResolution(ReportSector.report)(payload));

      const expected: TemporalReportState = {...initialState, resolution: payload};
      expect(state).toEqual(expected);
    });
  });

  describe('toggleComparePeriod', () => {

    it('toggles on', () => {
      const state: TemporalReportState = temporal(initialState, toggleComparePeriod(ReportSector.report)());

      const expected: TemporalReportState = {...initialState, shouldComparePeriod: true};
      expect(state).toEqual(expected);
    });

    it('toggles off', () => {
      const state: TemporalReportState =
        temporal({...initialState, shouldComparePeriod: true}, toggleComparePeriod(ReportSector.report)());

      const expected: TemporalReportState = {...initialState, shouldComparePeriod: false};
      expect(state).toEqual(expected);
    });
  });

  describe('set timePeriod', () => {

    const thresholdQuery: ThresholdQuery = {
      ...makeThreshold(),
      dateRange: {period: Period.yesterday},
    };

    const userSelection: UserSelection = userSelectionState.userSelection;

    describe('setThreshold', () => {

      it('will set time period from threshold payload', () => {
        const expected: TemporalReportState = {...initialState, timePeriod: {period: Period.yesterday}};
        expect(temporal(initialState, setThreshold(thresholdQuery))).toEqual(expected);
      });

      it('will reset to initial state when threshold is cleared', () => {
        const emptyThresholdQuery: ThresholdQuery = {...thresholdQuery, value: ''};

        expect(temporal(initialState, setThreshold(emptyThresholdQuery))).toEqual(initialState);
      });
    });

    describe('selectSavedSelection', () => {

      it('will set time period from selected user selection threshold', () => {
        const payload: UserSelection = {
          ...userSelection,
          selectionParameters: {
            ...userSelection.selectionParameters,
            threshold: thresholdQuery
          }
        };
        const expected: TemporalReportState = {...initialState, timePeriod: {period: Period.yesterday}};

        expect(temporal(initialState, selectSavedSelectionAction(payload))).toEqual(expected);
      });

      it('will use old time period when selected selection does not have a threshold time period set', () => {
        const state: TemporalReportState = {...initialState, timePeriod: {period: Period.currentMonth}};

        const nextState = temporal(state, selectSavedSelectionAction(userSelection));

        expect(nextState).toBe(state);
      });

      it('will reset to initial state when threshold is cleared', () => {
        const state: TemporalReportState = {...initialState, timePeriod: {period: Period.currentMonth}};
        const emptyThresholdQuery: ThresholdQuery = {...thresholdQuery, value: ''};

        const payload: UserSelection = {
          ...userSelection,
          selectionParameters: {
            ...userSelection.selectionParameters,
            threshold: emptyThresholdQuery
          }
        };

        expect(temporal(state, selectSavedSelectionAction(payload))).toBe(initialState);
      });
    });
  });

});
