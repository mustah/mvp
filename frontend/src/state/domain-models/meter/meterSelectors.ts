import {normalize} from 'normalizr';
import {createSelector} from 'reselect';
import {PieData, PieSlice} from '../../../components/pie-chart-selector/PieChartSelector';
import {pieChartTranslation} from '../../../helpers/translations';
import {IdNamed, Maybe, uuid} from '../../../types/Types';
import {FilterParam, ParameterName} from '../../search/selection/selectionModels';
import {DomainModel} from '../domainModels';
import {getResultDomainModels} from '../domainModelsSelectors';
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

export const getMetersTotal = (state: MetersState): number => state.total;
export const getMeterEntities = (state: MetersState): DomainModel<Meter> => state.entities;

export const getSelectionTree = createSelector<MetersState, uuid[], DomainModel<Meter>, SelectionTreeData>(
  getResultDomainModels,
  getMeterEntities,
  (meterIds: uuid[], metersDict: DomainModel<Meter>) => {

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
  let label: uuid;
  let existentEntity: Maybe<PieSlice>;
  let value: number;

  const categoryAdd = (fieldKey: MeterDataSummaryKey, idNamed: IdNamed, filterParam: FilterParam): PieSlice => ({
    name: pieChartTranslation(fieldKey, idNamed),
    value,
    filterParam,
  });

  switch (fieldKey) {
    case 'flagged':
      label = meter[fieldKey] ? 'flagged' : 'unFlagged';
      existentEntity = category[label];
      value = existentEntity ? ++existentEntity.value : 1;
      return {
        ...category,
        [label]: categoryAdd(fieldKey, {id: label, name: label}, meter[fieldKey]),
      };

    case 'city':
    case 'status':
      label = meter[fieldKey].id;
      existentEntity = category[label];
      value = existentEntity ? ++existentEntity.value : 1;
      return {
        ...category,
        [label]: categoryAdd(fieldKey, meter[fieldKey], label),
      };

    default:
      label = meter[fieldKey];
      existentEntity = category[label];
      value = existentEntity ? ++existentEntity.value : 1;
      return {
        ...category,
        [label]: categoryAdd(fieldKey, {id: label, name: label}, label),
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

export const getMeterDataSummary = createSelector<MetersState, uuid[], DomainModel<Meter>, Maybe<MeterDataSummary>>(
  getResultDomainModels,
  getMeterEntities,
  (meterIds: uuid[], meters: DomainModel<Meter>) => {
    const summaryTemplate: {[P in MeterDataSummaryKey]: PieData} = {
      flagged: {}, city: {}, manufacturer: {}, medium: {}, status: {}, alarm: {},
    };
    if (!meterIds.length) {
      return null;
    } else {
      return meterIds.reduce((summary, meterId: uuid) => {
        const meter = meters[meterId];
        return Object.keys(summaryTemplate).reduce(
          (summaryAggregated, fieldKey: MeterDataSummaryKey) =>
            addMeterDataToSummary(summaryAggregated, fieldKey, meter), summary);
      }, summaryTemplate);
    }
  },
);
