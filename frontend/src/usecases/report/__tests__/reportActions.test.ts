import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {RootState} from '../../../reducers/rootReducer';
import {allQuantities, Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {addAllToReport, addToReport, deleteItem, hideAllLines, setSelectedItems} from '../reportActions';
import {LegendItem} from '../reportModels';
import {initialState as initialReportState} from '../reportReducer';

describe('reportActions', () => {
  type PartialRootState = Pick<RootState, 'report'> ;

  const configureMockStore: (state: PartialRootState) => any = configureStore([thunk]);

  let initialState: PartialRootState;

  const legendItem: LegendItem = {id: 0, label: '1', medium: Medium.gas};
  const districtHeatingLegendItem: LegendItem = {id: 5, label: '5', medium: Medium.districtHeating};
  const unknownLegendItem: LegendItem = {id: 9, label: '9', medium: Medium.unknown};

  const items: LegendItem[] = [
    {id: 1, label: 'a', medium: Medium.gas},
    {id: 2, label: 'b', medium: Medium.water}
  ];

  beforeEach(() => {
    initialState = {report: initialReportState};
  });

  describe('addToReport', () => {

    it('adds a meter that is not already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(legendItem));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [legendItem],
          media: [Medium.gas],
          quantities: [Quantity.volume]
        })
      ]);
    });

    it('does not fire an event if meter is already selected', () => {
      const store = configureMockStore({
        ...initialState,
        report: {
          ...initialReportState,
          savedReports: {meterPage: {id: 'meterPage', meters: items}},
        }
      });

      store.dispatch(addToReport(items[0]));

      expect(store.getActions()).toEqual([]);
    });

    it('selects report indicators', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(legendItem));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [legendItem],
          media: [Medium.gas],
          quantities: [Quantity.volume]
        })
      ]);
    });

    it('does not add unknown medium meter to graph', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(unknownLegendItem));

      expect(store.getActions()).toEqual([
        setSelectedItems({items: [], media: [], quantities: []})
      ]);
    });

    it('adds new id to selected with already selected non-default quantity', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(districtHeatingLegendItem));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [districtHeatingLegendItem],
          media: [Medium.districtHeating],
          quantities: allQuantities[Medium.districtHeating]
        })
      ]);
    });

    it('adds more than one legend item to report', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(legendItem));
      store.dispatch(addToReport(districtHeatingLegendItem));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [legendItem],
          quantities: [Quantity.volume],
          media: [Medium.gas]
        }),
        setSelectedItems({
          items: [districtHeatingLegendItem],
          media: [Medium.districtHeating],
          quantities: allQuantities[Medium.districtHeating]
        })
      ]);
    });
  });

  describe('addAllToReport', () => {

    it('shows all meters in graph', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addAllToReport(items));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items,
          media: [Medium.gas, Medium.water],
          quantities: [Quantity.volume]
        })
      ]);
    });

    it('excludes meters with unknown medium', () => {
      const store = configureMockStore(initialState);

      const payloadItems: LegendItem[] = [...items, {id: 3, label: 'u', medium: Medium.unknown}];

      store.dispatch(addAllToReport(payloadItems));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items,
          media: [Medium.gas, Medium.water],
          quantities: [Quantity.volume]
        })
      ]);
    });

    it('duplicate meter ids are removed', () => {
      const store = configureMockStore(initialState);
      const payloadItems: LegendItem[] = [...items, {...items[0]}];

      store.dispatch(addAllToReport(payloadItems));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items,
          media: [Medium.gas, Medium.water],
          quantities: [Quantity.volume]
        })
      ]);
    });

    it('appends items to report', () => {
      const store = configureMockStore({
        ...initialState,
        report: {
          ...initialReportState,
          savedReports: {meterPage: {id: 'meterPage', meters: items}},
        }
      });

      store.dispatch(addAllToReport([legendItem]));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [...items, legendItem],
          media: [Medium.gas, Medium.water],
          quantities: [Quantity.volume]
        })
      ]);
    });

  });

  describe('deleteItem', () => {

    it('deletes item by id', () => {
      const store = configureMockStore({
        ...initialState,
        report: {
          ...initialReportState,
          savedReports: {meterPage: {id: 'meterPage', meters: items}},
        }
      });

      store.dispatch(deleteItem(1));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [items[1]],
          media: [Medium.water],
          quantities: [Quantity.volume]
        })
      ]);
    });

    it('does nothing when id to remove does not exist', () => {
      const store = configureMockStore({
        ...initialState,
        report: {
          ...initialReportState,
          savedReports: {meterPage: {id: 'meterPage', meters: items}},
        }
      });

      store.dispatch(deleteItem(888));

      expect(store.getActions()).toEqual([]);
    });
  });

  describe('hideAllLines', () => {

    it('dispatches hide all lines action creator', () => {
      const store = configureMockStore(initialState);

      store.dispatch(hideAllLines());

      expect(store.getActions()).toEqual([hideAllLines()]);
    });
  });

  describe('save report', () => {

    it('saves to current report', () => {
      const store = configureMockStore({...initialState});
      const items: LegendItem[] = [{id: 1, label: 'a', medium: Medium.electricity}];

      store.dispatch(addAllToReport(items));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items,
          media: [Medium.electricity],
          quantities: allQuantities[Medium.electricity],
        }),
      ]);
    });
  });
});
