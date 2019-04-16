import {makeThreshold} from '../../../__tests__/testDataFactory';
import {Period, TemporalResolution} from '../../../components/dates/dateModels';
import {Medium} from '../../ui/graph/measurement/measurementModels';
import {selectPeriod, selectSavedSelectionAction, setThreshold} from '../../user-selection/userSelectionActions';
import {SelectionInterval, ThresholdQuery, UserSelection} from '../../user-selection/userSelectionModels';
import {initialState as userSelecectionState} from '../../user-selection/userSelectionReducer';
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
      const action = setReportTimePeriod(ReportSector.report)({period: Period.currentMonth});

      const afterChange: TemporalReportState = temporal(initialState, action);

      const expected: SelectionInterval = {period: Period.currentMonth};
      expect(afterChange.timePeriod).toEqual(expected);
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
      dateRange: {period: Period.currentWeek},
    };

    const userSelection: UserSelection = userSelecectionState.userSelection;

    describe('setThreshold', () => {

      it('will set time period from threshold payload', () => {
        const expected: TemporalReportState = {...initialState, timePeriod: {period: Period.currentWeek}};
        expect(temporal(initialState, setThreshold(thresholdQuery))).toEqual(expected);
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
        const expected: TemporalReportState = {...initialState, timePeriod: {period: Period.currentWeek}};

        expect(temporal(initialState, selectSavedSelectionAction(payload))).toEqual(expected);
      });

      it('will reset time period to latest when selected saved selection without threshold', () => {
        const state: TemporalReportState = {...initialState, timePeriod: {period: Period.currentMonth}};

        const expected: TemporalReportState = {...initialState, timePeriod: {period: Period.latest}};

        expect(temporal(state, selectSavedSelectionAction(userSelection))).toEqual(expected);
      });
    });
  });

});
