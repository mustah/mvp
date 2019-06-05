import {makeThreshold} from '../../../__tests__/testDataFactory';
import {Period, TemporalResolution} from '../../../components/dates/dateModels';
import {Medium} from '../../ui/graph/measurement/measurementModels';
import {selectPeriod, selectSavedSelectionAction, setThreshold} from '../../user-selection/userSelectionActions';
import {SelectionInterval, ThresholdQuery, UserSelection} from '../../user-selection/userSelectionModels';
import {initialState as userSelectionState} from '../../user-selection/userSelectionReducer';
import {addLegendItems, selectResolution, setReportTimePeriod, toggleComparePeriod} from '../reportActions';
import {LegendItem, ReportSector, TemporalReportState} from '../reportModels';
import {initialState, temporalReducerFor} from '../temporalReducer';

describe('temporal', () => {

  const temporal = temporalReducerFor(ReportSector.report);

  const legendItems: LegendItem[] = [
    {id: 1, label: 'a', type: Medium.gas, isHidden: false, quantities: []},
    {id: 2, label: 'b', type: Medium.water, isHidden: false, quantities: []}
  ];

  describe('change period', () => {

    it('should not clear selected list items when changing global period', () => {
      const state: TemporalReportState = temporal(initialState, addLegendItems(ReportSector.report)(legendItems));

      expect(state).toEqual(initialState);

      const newState: TemporalReportState = temporal(state, selectPeriod(Period.currentMonth));
      expect(newState).toBe(state);
    });

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

      it('will reset time period to yesterday when selected saved selection without threshold', () => {
        const state: TemporalReportState = {...initialState, timePeriod: {period: Period.currentMonth}};

        const expected: TemporalReportState = {...initialState, timePeriod: {period: Period.yesterday}};

        expect(temporal(state, selectSavedSelectionAction(userSelection))).toEqual(expected);
      });

      it('will reset to initial state when threshold is cleared', () => {
        const emptyThresholdQuery: ThresholdQuery = {...thresholdQuery, value: ''};

        const payload: UserSelection = {
          ...userSelection,
          selectionParameters: {
            ...userSelection.selectionParameters,
            threshold: emptyThresholdQuery
          }
        };

        expect(temporal(initialState, selectSavedSelectionAction(payload))).toEqual(initialState);
      });
    });
  });

});
