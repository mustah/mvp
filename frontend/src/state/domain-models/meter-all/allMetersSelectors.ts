import {normalize} from 'normalizr';
import {createSelector} from 'reselect';
import {PieData, PieSlice} from '../../../components/pie-chart-selector/PieChartSelector';
import {hasItems} from '../../../helpers/functionalHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {pieChartTranslation} from '../../../helpers/translations';
import {Dictionary, IdNamed, uuid} from '../../../types/Types';
import {Meter} from '../../domain-models-paginated/meter/meterModels';
import {selectionTreeSchema} from '../../domain-models-paginated/meter/meterSchema';
import {FilterParam, ParameterName} from '../../search/selection/selectionModels';
import {NormalizedState, ObjectsById} from '../domainModels';
import {getEntitiesDomainModels, getResultDomainModels} from '../domainModelsSelectors';
import {
  MeterDataSummary,
  MeterDataSummaryKey,
  SelectionTreeData,
  SelectionTreeItem,
  SelectionTreeItemProps,
  SelectionTreeItemsProps,
} from './allMetersModels';

export const getSelectionTree =
  createSelector<NormalizedState<Meter>, uuid[], ObjectsById<Meter>, SelectionTreeData>(
    getResultDomainModels,
    getEntitiesDomainModels,
    (meterIds: uuid[], metersDict: ObjectsById<Meter>) => {

      const selectionTree: {[key: string]: SelectionTreeItem[]} = {
        cities: [], addresses: [], addressClusters: [], meters: [],
      };
      const cities = new Set<uuid>();
      const addressClusters = new Set<uuid>();
      const addresses = new Set<uuid>();
      const meters = new Set<uuid>();

      meterIds.map((meterId: uuid) =>
        metersDict[meterId]).filter((meter) => meter !== undefined).forEach((meterEntity: Meter) => {

        const {location: {city, address}, facility} = meterEntity;
        const clusterName = address.name[0].toUpperCase();
        const clusterId = city.name + ':' + clusterName;
        const cluster: IdNamed = {id: clusterId, name: clusterName};
        const meter: IdNamed = {id: meterEntity.id as string, name: facility as string};

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

const selectionTreeItems = (
  selectionTree: Dictionary<SelectionTreeItem[]>,
  props: SelectionTreeItemsProps,
) => {
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

const addToPie = (pie: PieData, fieldKey: MeterDataSummaryKey, meter: Meter): PieData => {

  const sliceUpdate =
    (
      fieldKey: MeterDataSummaryKey,
      idNamed: IdNamed,
      filterParam: FilterParam,
      value: number,
    ): PieSlice => ({
      name: pieChartTranslation(fieldKey, idNamed),
      value,
      filterParam,
    });

  const initOrIncrease = (pieSlice: PieSlice): number => {
    return Maybe.maybe<PieSlice>(pieSlice)
      .map((pieSlice: PieSlice) => ++pieSlice.value)
      .orElse(1);
  };

  let label: uuid;
  let sliceObject;

  switch (fieldKey) {
    case 'flagged':
      label = meter[fieldKey] ? 'flagged' : 'unFlagged';
      return {
        ...pie,
        [label]: sliceUpdate(fieldKey, {
          id: label,
          name: label,
        }, meter[fieldKey], initOrIncrease(pie[label])),
      };

    case 'location':
      label = (meter[fieldKey] && meter[fieldKey].city.id)  || 'unknown';
      sliceObject = meter[fieldKey] ? meter[fieldKey].city : {id: 'unknown', name: 'unknown'};
      return {
        ...pie,
        [label]: sliceUpdate(fieldKey, sliceObject as IdNamed, label, initOrIncrease(pie[label])),
      };
    case 'status':
      label = meter[fieldKey] && (meter[fieldKey].id || meter[fieldKey].id === 0) ? meter[fieldKey].id : 'unknown';
      sliceObject = meter[fieldKey] ? meter[fieldKey] : {id: 'unknown', name: 'unknown'};
      return {
        ...pie,
        [label]: sliceUpdate(fieldKey, sliceObject as IdNamed, label, initOrIncrease(pie[label])),
      };
    default:
      label = meter[fieldKey];
      return {
        ...pie,
        [label]: sliceUpdate(fieldKey, {id: label, name: label}, label, initOrIncrease(pie[label])),
      };
  }
};

const createDataSummary = (
  summary,
  fieldKey: MeterDataSummaryKey,
  meter: Meter,
): MeterDataSummary => {
  const category: PieData = summary[fieldKey];
  return {
    ...summary,
    [fieldKey]: {
      ...addToPie(category, fieldKey, meter),
    },
  };
};

export const getMeterDataSummary =
  createSelector<NormalizedState<Meter>, uuid[], ObjectsById<Meter>, Maybe<MeterDataSummary>>(
    getResultDomainModels,
    getEntitiesDomainModels,
    (metersIds: uuid[], meters: ObjectsById<Meter>): Maybe<MeterDataSummary> => {
      const summaryTemplate: {[P in MeterDataSummaryKey]: PieData} = {
        flagged: {}, location: {}, manufacturer: {}, medium: {}, status: {}, alarm: {},
      };

      return Maybe.just<uuid[]>(metersIds)
        .filter(hasItems)
        .flatMap(() => Maybe.just<MeterDataSummary>(
          metersIds.reduce((summary: MeterDataSummary, meterId: uuid) => {
            const meter = meters[meterId];
            return Object.keys(summaryTemplate)
              .reduce((summaryAggregated, fieldKey: MeterDataSummaryKey) =>
                createDataSummary(summaryAggregated, fieldKey, meter), summary);
          }, summaryTemplate),
        ));
    },
  );
