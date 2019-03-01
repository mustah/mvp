import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {savedReportsWith} from '../../../__tests__/testDataFactory';
import {RootState} from '../../../reducers/rootReducer';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {addAllToReport, addLegendItems, addToReport, deleteItem} from '../reportActions';
import {LegendItem, SavedReportsState} from '../reportModels';
import {initialState as report} from '../reportReducer';

describe('reportActions', () => {
  type PartialRootState = Pick<RootState, 'report'> ;

  const configureMockStore: (state: PartialRootState) => any = configureStore([thunk]);

  let initialState: PartialRootState;

  const isHidden = false;
  const quantities: Quantity[] = [];

  const gasLegendItem: LegendItem = {id: 0, label: '1', type: Medium.gas, isHidden, quantities};
  const districtHeatingLegendItem: LegendItem = {
    id: 5,
    label: '5',
    type: Medium.districtHeating,
    isHidden,
    quantities
  };
  const unknownLegendItem: LegendItem = {id: 9, label: '9', type: Medium.unknown, isHidden, quantities};

  const items: LegendItem[] = [
    {id: 1, label: 'a', type: Medium.gas, isHidden, quantities},
    {id: 2, label: 'b', type: Medium.water, isHidden, quantities}
  ];

  const savedReports: SavedReportsState = savedReportsWith(items);

  beforeEach(() => {
    initialState = {report};
  });

  describe('addToReport', () => {

    it('adds a meter that is not already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(gasLegendItem));

      expect(store.getActions()).toEqual([addLegendItems([gasLegendItem])]);
    });

    it('does not fire an event if meter is already selected', () => {
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports}
      });

      store.dispatch(addToReport(items[0]));

      expect(store.getActions()).toEqual([]);
    });

    it('selects report indicators', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(gasLegendItem));

      expect(store.getActions()).toEqual([addLegendItems([gasLegendItem])]);
    });

    it('does not add unknown type meter to graph', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(unknownLegendItem));

      expect(store.getActions()).toEqual([]);
    });

    it('adds new id to selected with already selected non-default quantity', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(districtHeatingLegendItem));

      expect(store.getActions()).toEqual([addLegendItems([districtHeatingLegendItem])]);
    });

    it('adds more than one legend item to report', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(gasLegendItem));
      store.dispatch(addToReport(districtHeatingLegendItem));

      expect(store.getActions()).toEqual([
        addLegendItems([gasLegendItem]),
        addLegendItems([districtHeatingLegendItem])
      ]);
    });

    it('copies the view settings for the same type', () => {
      const meters: LegendItem[] = [{...gasLegendItem, isRowExpanded: true}];
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports: savedReportsWith(meters)}
      });

      const newGasLegendItem: LegendItem = {...gasLegendItem, id: 2};
      store.dispatch(addToReport(newGasLegendItem));

      expect(store.getActions()).toEqual([addLegendItems([meters[0], {...newGasLegendItem, isRowExpanded: true}])]);
    });
  });

  describe('addAllToReport', () => {

    it('shows all meters in graph', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addAllToReport(items));

      expect(store.getActions()).toEqual([addLegendItems(items)]);
    });

    it('excludes meters with unknown type', () => {
      const store = configureMockStore(initialState);

      const payloadItems: LegendItem[] = [
        ...items,
        {id: 3, label: 'u', type: Medium.unknown, isHidden, quantities: []}
      ];

      store.dispatch(addAllToReport(payloadItems));

      expect(store.getActions()).toEqual([addLegendItems(items)]);
    });

    it('duplicate meter ids are removed', () => {
      const store = configureMockStore(initialState);
      const payloadItems: LegendItem[] = [...items, {...items[0]}];

      store.dispatch(addAllToReport(payloadItems));

      expect(store.getActions()).toEqual([addLegendItems(items)]);
    });

    it('appends items to report', () => {
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports}
      });

      store.dispatch(addAllToReport([gasLegendItem]));

      expect(store.getActions()).toEqual([addLegendItems([...items, gasLegendItem])]);
    });

  });

  describe('deleteItem', () => {

    it('deletes item by id', () => {
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports}
      });

      store.dispatch(deleteItem(1));

      expect(store.getActions()).toEqual([addLegendItems([items[1]])]);
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

  describe('save report', () => {

    it('saves to current report', () => {
      const store = configureMockStore({...initialState});

      store.dispatch(addAllToReport(items));

      expect(store.getActions()).toEqual([addLegendItems(items)]);
    });
  });
});
