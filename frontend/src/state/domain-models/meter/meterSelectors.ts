import {normalize} from 'normalizr';
import {createSelector} from 'reselect';
import {PieData, PieSlice} from '../../../components/pie-chart-selector/PieChartSelector';
import {hasItems} from '../../../helpers/functionalHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {pieChartTranslation} from '../../../helpers/translations';
import {IdNamed, uuid} from '../../../types/Types';
import {FilterParam, ParameterName} from '../../search/selection/selectionModels';
import {ObjectsById} from '../domainModels';
import {getResultDomainModels} from '../domainModelsSelectors';
import {HasComponentId} from '../../ui/pagination/paginationModels';
import {
  Meter,
  MeterDataSummary,
  MeterDataSummaryKey,
  MetersState,
  SelectionTreeData,
  SelectionTreeItem,
  SelectionTreeItemProps,
  SelectionTreeItemsProps,
} from './meterModels';
import {selectionTreeSchema} from './meterSchema';

type PaginatedMeterState = MetersState & HasComponentId;

export const getMeterEntities = ({entities}: PaginatedMeterState): ObjectsById<Meter> => entities;
// TODO: Make this function more rigid to missing fetched data.
export const getMeterResult = ({result, componentId}: PaginatedMeterState): uuid[] =>
  result[componentId] ? result[componentId].content : [];

export const getMetersIsFetching =
  ({result, componentId}: PaginatedMeterState): boolean => result[componentId] ? result[componentId].isFetching : false;

export const getSelectionTree = createSelector<MetersState, uuid[], ObjectsById<Meter>, SelectionTreeData>(
  getResultDomainModels,
  getMeterEntities,
  (meterIds: uuid[], metersDict: ObjectsById<Meter>) => {

    const selectionTree: {[key: string]: SelectionTreeItem[]} = {
      cities: [], addresses: [], addressClusters: [], meters: [],
    };
    const cities = new Set<uuid>();
    const addressClusters = new Set<uuid>();
    const addresses = new Set<uuid>();
    const meters = new Set<uuid>();

    meterIds.forEach((meterId: uuid) => {
      const meterRelations = metersDict[meterId];
      if (!meterRelations) {
        // Since we cannot use types to assure that all results included
        // in a normalized state, we must detect when a result is not in
        // the included entities in runtime.
        return;
      }
      const {city, address, facility} = meterRelations;
      const clusterName = address.name[0];
      const clusterId = city.name + ':' + clusterName;
      const cluster: IdNamed = {id: clusterId, name: clusterName};
      const meter: IdNamed = {id: meterId as string, name: facility as string};

      selectionTreeItems(selectionTree, {
        category: ParameterName.cities,
        set: cities,
        unit: city,
        parentType: '',
        parent: {id: '', name: ''},
        selectable: true,
        childrenType: 'addressClusters',
      });

      selectionTreeItems(selectionTree, {
        category: 'addressClusters',
        set: addressClusters,
        unit: cluster,
        parentType: ParameterName.cities,
        parent: city,
        selectable: false,
        childrenType: ParameterName.addresses,
      });

      selectionTreeItems(selectionTree, {
        category: ParameterName.addresses,
        set: addresses,
        unit: address,
        parentType: 'addressClusters',
        parent: cluster,
        selectable: true,
        childrenType: 'meters',
      });

      selectionTreeItems(selectionTree, {
        category: 'meters',
        set: meters,
        unit: meter,
        parentType: ParameterName.addresses,
        parent: address,
        selectable: true,
        childrenType: '',
      });
    });
    // TODO: Perhaps move this moderation into the selectionTreeItemsList to speed up performance.
    selectionTree.addressClusters.forEach((item) => {
      item.name = item.name + '...(' + item.childNodes.ids.length + ')';
    });

    return normalize(selectionTree, selectionTreeSchema);
  },
);

const selectionTreeItem =
  (props: SelectionTreeItemProps): SelectionTreeItem => {
    return {
      id: props.unit.id,
      name: props.unit.name,
      parent: {type: props.parentType, id: props.parent.id},
      selectable: props.selectable,
      childNodes: {type: props.childrenType, ids: []},
    };
  };

const selectionTreeItems = (selectionTree: {[key: string]: SelectionTreeItem[]}, props: SelectionTreeItemsProps) => {
  const {category, set, ...selectionTreeItemProps} = props;
  const {unit, parent, parentType} = props;
  if (!set.has(unit.id)) {

    selectionTree[category].push(selectionTreeItem(selectionTreeItemProps));
    set.add(unit.id);

    if (parentType !== '') {
      selectionTree[parentType].map((par) => {
        if (par.id === parent.id) {
          par.childNodes.ids.push(unit.id);
        }
      });
    }
  }
};

const addToCategory = (category: PieData, fieldKey: MeterDataSummaryKey, meter: Meter): PieData => {

  const categoryAdd =
    (fieldKey: MeterDataSummaryKey, idNamed: IdNamed, filterParam: FilterParam, value: number): PieSlice => ({
      name: pieChartTranslation(fieldKey, idNamed),
      value,
      filterParam,
    });

  const valueOf = (pieSlice: PieSlice): number => {
    return Maybe.maybe<PieSlice>(pieSlice)
      .map((pieSlice: PieSlice) => ++pieSlice.value)
      .orElse(1);
  };

  let label: uuid;

  switch (fieldKey) {
    case 'flagged':
      label = meter[fieldKey] ? 'flagged' : 'unFlagged';
      return {
        ...category,
        [label]: categoryAdd(fieldKey, {
          id: label,
          name: label,
        }, meter[fieldKey], valueOf(category[label])),
      };

    case 'city':
    case 'status':
      label = meter[fieldKey].id;
      return {
        ...category,
        [label]: categoryAdd(fieldKey, meter[fieldKey], label, valueOf(category[label])),
      };

    default:
      label = meter[fieldKey];
      return {
        ...category,
        [label]: categoryAdd(fieldKey, {id: label, name: label}, label, valueOf(category[label])),
      };
  }
};

const addMeterDataToSummary = (summary, fieldKey: MeterDataSummaryKey, meter: Meter): MeterDataSummary => {
  const category: PieData = summary[fieldKey];
  return {
    ...summary,
    [fieldKey]: {
      ...addToCategory(category, fieldKey, meter),
    },
  };
};

export const getMeterDataSummary = createSelector<MetersState, uuid[], ObjectsById<Meter>, Maybe<MeterDataSummary>>(
  getResultDomainModels,
  getMeterEntities,
  (metersIds: uuid[], meters: ObjectsById<Meter>): Maybe<MeterDataSummary> => {
    const summaryTemplate: {[P in MeterDataSummaryKey]: PieData} = {
      flagged: {}, city: {}, manufacturer: {}, medium: {}, status: {}, alarm: {},
    };

    return Maybe.just<uuid[]>(metersIds)
      .filter(hasItems)
      .flatMap(() => Maybe.just<MeterDataSummary>(
        metersIds.reduce((summary: MeterDataSummary, meterId: uuid) => {
          const meter = meters[meterId];
          return Object.keys(summaryTemplate)
            .reduce((summaryAggregated, fieldKey: MeterDataSummaryKey) =>
              addMeterDataToSummary(summaryAggregated, fieldKey, meter), summary);
        }, summaryTemplate),
      ));
  },
);
