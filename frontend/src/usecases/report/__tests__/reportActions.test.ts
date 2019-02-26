import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {savedReportsOf} from '../../../__tests__/testDataFactory';
import {RootState} from '../../../reducers/rootReducer';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {allQuantities, Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {addAllToReport, addToReport, deleteItem, setSelectedItems} from '../reportActions';
import {LegendItem, Report} from '../reportModels';
import {initialState as report} from '../reportReducer';

describe('reportActions', () => {
  type PartialRootState = Pick<RootState, 'report'> ;

  const configureMockStore: (state: PartialRootState) => any = configureStore([thunk]);

  let initialState: PartialRootState;

  const isHidden = false;
  const gasLegendItem: LegendItem = {id: 0, label: '1', medium: Medium.gas, isHidden};
  const districtHeatingLegendItem: LegendItem = {id: 5, label: '5', medium: Medium.districtHeating, isHidden};
  const unknownLegendItem: LegendItem = {id: 9, label: '9', medium: Medium.unknown, isHidden};

  const items: LegendItem[] = [
    {id: 1, label: 'a', medium: Medium.gas, isHidden},
    {id: 2, label: 'b', medium: Medium.water, isHidden}
  ];

  const savedReports: ObjectsById<Report> = savedReportsOf(items);

  beforeEach(() => {
    initialState = {report};
  });

  describe('addToReport', () => {

    it('adds a meter that is not already selected', () => {
      const store = configureMockStore(initialState);

      store.dispatch(addToReport(gasLegendItem));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [gasLegendItem],
          media: [Medium.gas],
          quantities: [Quantity.volume]
        })
      ]);
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

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [gasLegendItem],
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

      store.dispatch(addToReport(gasLegendItem));
      store.dispatch(addToReport(districtHeatingLegendItem));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [gasLegendItem],
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

    it('copies the view settings for the same medium', () => {
      const meters: LegendItem[] = [{...gasLegendItem, isRowExpanded: true}];
      const store = configureMockStore({
        ...initialState,
        report: {...report, savedReports: savedReportsOf(meters)}
      });

      const newGasLegendItem: LegendItem = {...gasLegendItem, id: 2};
      store.dispatch(addToReport(newGasLegendItem));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [meters[0], {...newGasLegendItem, isRowExpanded: true}],
          media: [Medium.gas],
          quantities: allQuantities[Medium.gas]
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

      const payloadItems: LegendItem[] = [...items, {id: 3, label: 'u', medium: Medium.unknown, isHidden}];

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
        report: {...report, savedReports}
      });

      store.dispatch(addAllToReport([gasLegendItem]));

      expect(store.getActions()).toEqual([
        setSelectedItems({
          items: [...items, gasLegendItem],
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
        report: {...report, savedReports}
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
        report: {...report, savedReports}
      });

      store.dispatch(deleteItem(888));

      expect(store.getActions()).toEqual([]);
    });
  });

  describe('save report', () => {

    it('saves to current report', () => {
      const store = configureMockStore({...initialState});
      const items: LegendItem[] = [{id: 1, label: 'a', medium: Medium.electricity, isHidden}];

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
