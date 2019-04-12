import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {savedReportsWith} from '../../../__tests__/testDataFactory';
import {RootState} from '../../../reducers/rootReducer';
import {toLegendItem} from '../../../usecases/report/helpers/legendHelper';
import {Meter} from '../../domain-models-paginated/meter/meterModels';
import {getMediumText, Medium, Quantity} from '../../ui/graph/measurement/measurementModels';
import {addAllToReport, addLegendItems, addToReport, deleteItem} from '../reportActions';
import {LegendItem, ReportSector, SavedReportsState} from '../reportModels';
import {initialState as report} from '../reportReducer';

describe('reportActions', () => {
  type PartialRootState = Pick<RootState, 'report'> ;

  const section: ReportSector = ReportSector.report;
  const configureMockStore: (state: PartialRootState) => any = configureStore([thunk]);

  let initialState: PartialRootState;

  const isHidden = false;
  const quantities: Quantity[] = [];

  const gasMeter: LegendItem = {id: 0, label: 'a', type: Medium.gas, isHidden, quantities};
  const waterMeter = {id: 1, label: 'b', type: Medium.water, isHidden, quantities};
  const districtHeatingMeter: LegendItem = {
    id: 5,
    label: '5',
    type: Medium.districtHeating,
    isHidden,
    quantities
  };
  const unknownLegendItem: LegendItem = {id: 9, label: '9', type: Medium.unknown, isHidden, quantities};

  const items: LegendItem[] = [gasMeter, waterMeter];

  const savedReports: SavedReportsState = savedReportsWith(items);

  beforeEach(() => {
    initialState = {report};
  });

  describe('addToReport', () => {

    beforeEach(() => {
      initialState = {report};
    });

    it('adds a meter that is not already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(gasMeter));

      expect(store.getActions()).toEqual([addLegendItems(section)([{...gasMeter, quantities: [Quantity.volume]}])]);
    });

    it('does not fire an event if meter is already selected', () => {
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports}
      });

      store.dispatch(addToReport(gasMeter));

      expect(store.getActions()).toEqual([]);
    });

    it('does not add unknown type meter to graph', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(unknownLegendItem));

      expect(store.getActions()).toEqual([]);
    });

    it('adds new id to selected with already selected non-default quantity', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(districtHeatingMeter));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([{...districtHeatingMeter, quantities: [Quantity.energy]}])
      ]);
    });

    it('appends legend item to the legend', () => {
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports}
      });

      store.dispatch(addToReport(districtHeatingMeter));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([...items, {...districtHeatingMeter, quantities: [Quantity.energy]}]),
      ]);
    });

    it('selects default quantity for given meter when there are no quantities selected', () => {
      const store = configureMockStore({...initialState});

      store.dispatch(addToReport(gasMeter));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([{...gasMeter, quantities: [Quantity.volume]}])
      ]);
    });

    it('selects default quantity for given meters', () => {
      const store = configureMockStore({...initialState});

      const newGasMeter = {...gasMeter, id: 6};

      store.dispatch(addToReport(gasMeter));
      store.dispatch(addToReport(newGasMeter));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([{...gasMeter, quantities: [Quantity.volume]}]),
        addLegendItems(section)([{...newGasMeter, quantities: [Quantity.volume]}])
      ]);
    });

    it('selects same quantities for same newly added meter type', () => {
      const meter1 = {...districtHeatingMeter, id: 6, quantities: [Quantity.power, Quantity.flow], isRowExpanded: true};
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports: savedReportsWith([meter1])}
      });

      const meter2 = {...districtHeatingMeter, id: 7};

      store.dispatch(addToReport(meter2));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([meter1, {...meter2, quantities: [Quantity.power, Quantity.flow], isRowExpanded: true}])
      ]);
    });

    it('selects same quantities for added same meter', () => {
      const store = configureMockStore({...initialState});

      const newGasMeter = {...gasMeter, id: 6};

      store.dispatch(addToReport(gasMeter));
      store.dispatch(addToReport(newGasMeter));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([{...gasMeter, quantities: [Quantity.volume]}]),
        addLegendItems(section)([{...newGasMeter, quantities: [Quantity.volume]}])
      ]);
    });

    it('does not selects default quantity when there are already two selected', () => {
      const roomMeter = {...gasMeter, id: 6, type: Medium.roomSensor};
      const legendItems = [
        {...gasMeter, quantities: [Quantity.volume]},
        {...districtHeatingMeter, quantities: [Quantity.energy]},
      ];

      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports: savedReportsWith(legendItems)}
      });

      store.dispatch(addToReport(roomMeter));

      expect(store.getActions()).toEqual([addLegendItems(section)([...legendItems, roomMeter])]);
    });

    it('copies the view settings for the same type', () => {
      const legendItems: LegendItem[] = [{...gasMeter, isRowExpanded: true}];
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports: savedReportsWith(legendItems)}
      });

      const newGasLegendItem: LegendItem = {...gasMeter, id: 2};
      store.dispatch(addToReport(newGasLegendItem));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([
          legendItems[0],
          {...newGasLegendItem, isRowExpanded: true, quantities: [Quantity.volume]}
        ])
      ]);
    });
  });

  describe('addAllToReport', () => {

    it('shows all meters in graph', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addAllToReport(items));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([
          {...gasMeter, quantities: [Quantity.volume]},
          {...waterMeter, quantities: [Quantity.volume]}
        ])
      ]);
    });

    it('excludes meters with unknown type', () => {
      const store = configureMockStore(initialState);

      const payloadItems: LegendItem[] = [
        ...items,
        {id: 3, label: 'u', type: Medium.unknown, isHidden, quantities: []}
      ];

      store.dispatch(addAllToReport(payloadItems));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([
          {...gasMeter, quantities: [Quantity.volume]},
          {...waterMeter, quantities: [Quantity.volume]}
        ])
      ]);
    });

    it('duplicate meter ids are not added', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addAllToReport([gasMeter, gasMeter]));

      expect(store.getActions()).toEqual([addLegendItems(section)([{...gasMeter, quantities: [Quantity.volume]}])]);
    });

    it('appends items to report', () => {
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports}
      });

      store.dispatch(addAllToReport([districtHeatingMeter]));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([
          ...items,
          {...districtHeatingMeter, quantities: [Quantity.energy]}
        ])
      ]);
    });

    it('selects default quantity for given meters', () => {
      const store = configureMockStore({...initialState});

      store.dispatch(addAllToReport([gasMeter]));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([{...gasMeter, quantities: [Quantity.volume]}])
      ]);
    });

    it('does not selects default quantity when there are already two selected', () => {
      const roomMeter = {...gasMeter, id: 6, type: Medium.roomSensor};
      const legendItems = [
        {...gasMeter, quantities: [Quantity.volume]},
        {...districtHeatingMeter, quantities: [Quantity.energy]},
      ];

      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports: savedReportsWith(legendItems)}
      });

      store.dispatch(addAllToReport([roomMeter]));

      expect(store.getActions()).toEqual([addLegendItems(section)([...legendItems, roomMeter])]);
    });

    it('selects default quantity for the first item in the list', () => {
      const roomMeter = {...gasMeter, id: 6, type: Medium.roomSensor};

      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports}
      });

      store.dispatch(addAllToReport([roomMeter, ...items]));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([
          ...items,
          {...roomMeter, quantities: [Quantity.externalTemperature]}
        ])
      ]);
    });

    it('copies the view settings for the same type for given group', () => {
      const meter1 = {...gasMeter, isRowExpanded: false};
      const meter2 = {...gasMeter, id: 2};
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports: savedReportsWith([meter1])}
      });

      store.dispatch(addAllToReport([meter2]));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([
          meter1,
          {...meter2, isRowExpanded: false, quantities: [Quantity.volume]}
        ])
      ]);
    });

    it('copies view settings where all lines are hidden', () => {
      const meter1: LegendItem = {...gasMeter, id: 2, isHidden: true, isRowExpanded: true};
      const meter = {id: 99, facility: 'abc', medium: getMediumText(Medium.gas)};
      const meter2 = toLegendItem(meter as Meter);
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports: savedReportsWith([meter1])}
      });

      store.dispatch(addAllToReport([meter2]));

      expect(store.getActions()).toEqual([
        addLegendItems(section)([
          {...meter1},
          {...meter2, isHidden: true, isRowExpanded: true, quantities: [Quantity.volume]}
        ])
      ]);
    });

  });

  describe('deleteItem', () => {

    it('deletes item by id', () => {
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports}
      });

      store.dispatch(deleteItem(1));

      expect(store.getActions()).toEqual([addLegendItems(section)([gasMeter])]);
    });

    it('does nothing when id to remove does not exist', () => {
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports}
      });

      store.dispatch(deleteItem(888));

      expect(store.getActions()).toEqual([]);
    });
  });

});
