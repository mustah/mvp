import {LegendItem} from '../../../usecases/report/reportModels';
import {ObjectsById} from '../../domain-models/domainModels';
import {Medium, Quantity} from '../../ui/graph/measurement/measurementModels';
import {SelectionTreeItemType, SelectionTreeViewComposite} from '../../ui/selection-tree/selectionTreeModels';
import {SelectionTreeMeter, SelectionTreeState} from '../selectionTreeModels';
import {initialState} from '../selectionTreeReducer';
import {getLegendItemsWithLimit, getSelectionTreeViewItems} from '../selectionTreeSelectors';

describe('selectionTreeSelectors', () => {

  const selectionTreeState: SelectionTreeState = {
    isFetching: false,
    isSuccessfullyFetched: true,
    entities: {
      cities: {
        'sweden,kungsbacka': {
          id: 'sweden,kungsbacka',
          city: 'sweden,kungsbacka',
          medium: [Medium.water],
          name: 'kungsbacka',
          addresses: ['sweden,kungsbacka,kabelgatan 2', 'sweden,kungsbacka,kabelgatan 3'],
        },
        'sweden,gothenburg': {
          id: 'sweden,gothenburg',
          city: 'sweden,gothenburg',
          medium: [Medium.water, Medium.gas],
          name: 'gothenburg',
          addresses: [
            'sweden,gothenburg,kungsgatan 2',
            'sweden,gothenburg,kungsgatan 1',
            'sweden,gothenburg,drottninggatan 1',
          ],
        },
        'denmark,copenhagen': {
          id: 'denmark,copenhagen',
          city: 'denmark,copenhagen',
          medium: [],
          name: 'copenhagen',
          addresses: ['denmark,copenhagen,kabelgatan 2'],
        },
      },
      addresses: {
        'sweden,kungsbacka,kabelgatan 2': {
          address: 'kabelgatan 2',
          city: 'sweden,kungsbacka',
          id: 'sweden,kungsbacka,kabelgatan 2',
          name: 'kabelgatan 2',
          meters: [1],
        },
        'sweden,kungsbacka,kabelgatan 3': {
          address: 'kabelgatan 3',
          city: 'sweden,kungsbacka',
          id: 'sweden,kungsbacka,kabelgatan 3',
          name: 'kabelgatan 3',
          meters: [2],
        },
        'sweden,gothenburg,kungsgatan 2': {
          address: 'kungsgatan 2',
          city: 'sweden,gothenburg',
          id: 'sweden,gothenburg,kungsgatan 2',
          name: 'kungsgatan 2',
          meters: [3],
        },
        'sweden,gothenburg,kungsgatan 1': {
          address: 'kungsgatan 1',
          city: 'sweden,gothenburg',
          id: 'sweden,gothenburg,kungsgatan 1',
          name: 'kungsgatan 1',
          meters: [4],
        },
        'sweden,gothenburg,drottninggatan 1': {
          address: 'drottninggatan 1',
          city: 'sweden,gothenburg',
          id: 'sweden,gothenburg,drottninggatan 1',
          name: 'drottninggatan 1',
          meters: [5],
        },
      },
      meters: {
        1: {
          address: 'kabelgatan 2',
          city: 'sweden,kungsbacka',
          id: 1,
          name: 'extId1',
          medium: Medium.water,
        },
        2: {
          address: 'kabelgatan 3',
          city: 'sweden,kungsbacka',
          id: 2,
          name: 'extId2',
          medium: Medium.water,
        },
        3: {
          address: 'kungsgatan 2',
          city: 'sweden,gothenburg',
          id: 3,
          name: 'extId3',
          medium: Medium.water,
        },
        4: {
          address: 'kungsgatan 1',
          city: 'sweden,gothenburg',
          id: 4,
          name: 'extId4',
          medium: Medium.gas,
        },
        5: {
          address: 'drottninggatan 1',
          city: 'sweden,gothenburg',
          id: 5,
          name: 'extId5',
          medium: Medium.gas,
        },
        6: {
          id: 6,
          address: 'kabelgatan 2',
          city: 'denmark,copenhagen',
          medium: Medium.unknown,
          name: 'ext6',
        },
      },
    },
    result: {
      cities: ['sweden,kungsbacka', 'sweden,gothenburg'],
    },
  };

  describe('getLegendItemsWithLimit', () => {

    const isHidden = false;
    const quantities: Quantity[] = [];

    it('handles no selection tree meters', () => {
      expect(getLegendItemsWithLimit()).toEqual([]);
    });

    it('handles empty selection tree meters', () => {
      const meters: ObjectsById<SelectionTreeMeter> = {};

      expect(getLegendItemsWithLimit(meters)).toEqual([]);
    });

    it('should only have one meter id', () => {
      const meters: ObjectsById<SelectionTreeMeter> = {
        1: {id: 1, name: 'a', address: 'b', city: 'c', medium: Medium.gas},
      };

      const expected: LegendItem[] = [{id: 1, label: 'a', type: Medium.gas, isHidden, quantities}];
      expect(getLegendItemsWithLimit(meters)).toEqual(expected);
    });

    it('should only have more than one meter id', () => {
      const meters: ObjectsById<SelectionTreeMeter> = {
        1: {id: 1, name: 'a', address: 'b', city: 'c', medium: Medium.gas},
        2: {id: 2, name: 'b', address: 'c', city: 'd', medium: Medium.water},
      };

      const expected: LegendItem[] = [
        {id: 1, label: 'a', type: Medium.gas, isHidden, quantities},
        {id: 2, label: 'b', type: Medium.water, isHidden, quantities}
      ];
      expect(getLegendItemsWithLimit(meters)).toEqual(expected);
    });
  });

  describe('getSelectionTreeViewItems', () => {

    it('has no items', () => {
      const items: SelectionTreeViewComposite[] = getSelectionTreeViewItems(initialState);
      expect(items).toEqual([]);
    });

    it('has no expanded items', () => {
      const items: SelectionTreeViewComposite[] = getSelectionTreeViewItems(selectionTreeState);

      const expected: SelectionTreeViewComposite[] = [
        {
          id: 'sweden,kungsbacka',
          text: 'kungsbacka',
          type: SelectionTreeItemType.city,
          items:
            [
              {
                id: 'sweden,kungsbacka,kabelgatan 2',
                text: 'kabelgatan 2',
                type: SelectionTreeItemType.address,
                items: [{id: 1, text: 'extId1', type: SelectionTreeItemType.meter, items: []}]
              },
              {
                id: 'sweden,kungsbacka,kabelgatan 3',
                text: 'kabelgatan 3',
                type: SelectionTreeItemType.address,
                items: [{id: 2, text: 'extId2', type: SelectionTreeItemType.meter, items: []}]
              }
            ]
        },
        {
          id: 'sweden,gothenburg',
          text: 'gothenburg',
          type: SelectionTreeItemType.city,
          items:
            [
              {
                id: 'sweden,gothenburg,drottninggatan 1',
                text: 'drottninggatan 1',
                type: SelectionTreeItemType.address,
                items: [{id: 5, text: 'extId5', type: SelectionTreeItemType.meter, items: []}]
              },
              {
                id: 'sweden,gothenburg,kungsgatan 1',
                text: 'kungsgatan 1',
                type: SelectionTreeItemType.address,
                items: [{id: 4, text: 'extId4', type: SelectionTreeItemType.meter, items: []}]
              },
              {
                id: 'sweden,gothenburg,kungsgatan 2',
                text: 'kungsgatan 2',
                type: SelectionTreeItemType.address,
                items: [{id: 3, text: 'extId3', type: SelectionTreeItemType.meter, items: []}]
              }
            ]
        }
      ];

      expect(items).toEqual(expected);
    });
  });

});
