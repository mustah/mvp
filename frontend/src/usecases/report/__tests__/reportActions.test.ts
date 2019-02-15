import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {RootState} from '../../../reducers/rootReducer';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {addToReport, deleteItem, hideAllLines, setSelectedItems, showMetersInGraph} from '../reportActions';
import {LegendItem, ReportState} from '../reportModels';

describe('reportActions', () => {
  type PartialRootState = Pick<RootState, 'report'> ;

  const configureMockStore: (state: PartialRootState) => any = configureStore([thunk]);

  let initialState: PartialRootState;

  const report: ReportState = {
    hiddenLines: [],
    resolution: TemporalResolution.day,
    savedReports: {}
  };

  const legendItem: LegendItem = {id: 0, label: '1', medium: Medium.gas};
  const districtHeatingLegendItem: LegendItem = {id: 5, label: '5', medium: Medium.districtHeating};
  const unknownLegendItem: LegendItem = {id: 9, label: '9', medium: Medium.unknown};

  const items: LegendItem[] = [
    {id: 1, label: 'a', medium: Medium.gas},
    {id: 2, label: 'b', medium: Medium.water}
  ];

  beforeEach(() => {
    initialState = {report};
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
          ...report,
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
          quantities: [Quantity.energy]
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
          quantities: [Quantity.energy],
          media: [Medium.districtHeating]
        })
      ]);
    });
  });

  describe('showMetersInGraph', () => {

    it('shows all meters in graph', () => {
      const store = configureMockStore({...initialState});

      store.dispatch(showMetersInGraph(items));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items,
          media: [Medium.gas, Medium.water],
          quantities: [Quantity.volume]
        })
      ]);
    });

    it('excludes meters with unknown medium', () => {
      const store = configureMockStore({...initialState});

      const payloadItems: LegendItem[] = [...items, {id: 3, label: 'u', medium: Medium.unknown}];

      store.dispatch(showMetersInGraph(payloadItems));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items,
          media: [Medium.gas, Medium.water],
          quantities: [Quantity.volume]
        })
      ]);
    });

    it('duplicate meter ids are removed', () => {
      const store = configureMockStore({...initialState});
      const payloadItems: LegendItem[] = [...items, {...items[0]}];

      store.dispatch(showMetersInGraph(payloadItems));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items,
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
          ...report,
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
          ...report,
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

      store.dispatch(showMetersInGraph(items));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items,
          media: [Medium.electricity],
          quantities: [Quantity.energy],
        }),
      ]);
    });
  });
});
