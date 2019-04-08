import {Period, TemporalResolution} from '../../../components/dates/dateModels';
import {Medium} from '../../ui/graph/measurement/measurementModels';
import {selectPeriod} from '../../user-selection/userSelectionActions';
import {SelectionInterval} from '../../user-selection/userSelectionModels';
import {
  addLegendItems,
  ReportSector,
  selectResolution,
  setReportTimePeriod,
  toggleComparePeriod
} from '../reportActions';
import {LegendItem, TemporalReportState} from '../reportModels';
import {initialState, temporalReducerFor} from '../temporalReducer';

describe('temporal', () => {

  const temporal = temporalReducerFor(ReportSector.report);
  const section: ReportSector = ReportSector.report;

  const legendItems: LegendItem[] = [
    {id: 1, label: 'a', type: Medium.gas, isHidden: false, quantities: []},
    {id: 2, label: 'b', type: Medium.water, isHidden: false, quantities: []}
  ];

  describe('change period', () => {

    it('should not clear selected list items when changing global period', () => {
      const state: TemporalReportState = temporal(initialState, addLegendItems(section)(legendItems));

      expect(state).toEqual(initialState);

      const newState: TemporalReportState = temporal(state, selectPeriod(Period.currentMonth));
      expect(newState).toBe(state);
    });

    it('can change its time period', () => {
      const action = setReportTimePeriod(section)({period: Period.currentMonth});

      const afterChange: TemporalReportState = temporal(initialState, action);

      const expected: SelectionInterval = {period: Period.currentMonth};
      expect(afterChange.timePeriod).toEqual(expected);
    });
  });

  describe('selectResolution', () => {

    it('can select hourly resolution', () => {
      const payload = TemporalResolution.hour;

      const state: TemporalReportState = temporal(initialState, selectResolution(section)(payload));

      const expected: TemporalReportState = {...initialState, resolution: payload};
      expect(state).toEqual(expected);
    });

    it('changes resolution', () => {
      const payload = TemporalResolution.hour;

      let state: TemporalReportState = temporal(initialState, selectResolution(section)(payload));

      let expected: TemporalReportState = {...initialState, resolution: payload};
      expect(state).toEqual(expected);

      state = temporal(initialState, selectResolution(section)(TemporalResolution.month));

      expected = {...initialState, resolution: TemporalResolution.month};
      expect(state).toEqual(expected);
    });
  });

  describe('toggleComparePeriod', () => {

    it('toggles on', () => {
      const state: TemporalReportState = temporal(initialState, toggleComparePeriod(section)());

      const expected: TemporalReportState = {...initialState, shouldComparePeriod: true};
      expect(state).toEqual(expected);
    });

    it('toggles off', () => {
      const state: TemporalReportState =
        temporal({...initialState, shouldComparePeriod: true}, toggleComparePeriod(section)());

      const expected: TemporalReportState = {...initialState, shouldComparePeriod: false};
      expect(state).toEqual(expected);
    });
  });

});