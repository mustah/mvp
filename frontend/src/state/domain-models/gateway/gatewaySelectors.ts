import {Pie, PieData} from '../../../components/pie-chart-selector/PieChartSelector';
import {pieChartTranslation} from '../../../services/translationService';
import {IdNamed, uuid} from '../../../types/Types';
import {FilterParam} from '../../search/selection/selectionModels';
import {DomainModel} from '../domainModels';
import {Gateway, GatewayDataSummary, GatewayDataSummaryKey, GatewaysState} from './gatewayModels';
import {createSelector} from 'reselect';
import {getResultDomainModels} from '../domainModelsSelectors';

export const getGatewaysTotal = (state: GatewaysState): number => state.total;
export const getGatewayEntities = (state: GatewaysState): DomainModel<Gateway> => state.entities;

const addToCategory = (category: PieData, fieldKey: GatewayDataSummaryKey, gateway: Gateway): PieData => {
  let label: uuid;
  let existentEntity: Pie | undefined;
  let value: number;

  const categoryAdd = (fieldKey: GatewayDataSummaryKey, idNamed: IdNamed, filterParam: FilterParam): Pie => ({
    name: pieChartTranslation(fieldKey, idNamed),
    value,
    filterParam,
  });

  switch (fieldKey) {
    case 'flagged':
      label = gateway[fieldKey] ? 'flagged' : 'unFlagged';
      existentEntity = category[label];
      value = existentEntity ? ++existentEntity.value : 1;
      return {
        ...category,
        [label]: categoryAdd(fieldKey, {id: label, name: label}, gateway[fieldKey]),
      };

    case 'city':
    case 'status':
      label = gateway[fieldKey].id;
      existentEntity = category[label];
      value = existentEntity ? ++existentEntity.value : 1;
      return {
        ...category,
        [label]: categoryAdd(fieldKey, gateway[fieldKey], label),
      };

    default:
      label = gateway[fieldKey];
      existentEntity = category[label];
      value = existentEntity ? ++existentEntity.value : 1;
      return {
        ...category,
        [label]: categoryAdd(fieldKey, {id: label, name: label as string}, label),
      };
  }
};

const addGatewayDataToSummary = (summary, fieldKey: GatewayDataSummaryKey, gateway: Gateway): GatewayDataSummary => {
  const category: PieData = summary[fieldKey];
  return {
    ...summary,
    [fieldKey]: {
      ...addToCategory(category, fieldKey, gateway),
    },
  };
};

export const getGatewayDataSummary =
  createSelector<GatewaysState, uuid[], DomainModel<Gateway>, GatewayDataSummary | null>(
    getResultDomainModels,
    getGatewayEntities,
    (gateways: uuid[], gatewayLookup: DomainModel<Gateway>) => {
      const summaryTemplate: {[P in GatewayDataSummaryKey]: PieData} = {
        status: {}, flagged: {}, city: {}, productModel: {},
      };
      if (!gateways.length) {
        return null;
      } else {
        return gateways.reduce((summary, gatewayId: uuid) => {
          const gateway = gatewayLookup[gatewayId];
          return Object.keys(summaryTemplate).reduce(
            (summaryAggregated, fieldKey: GatewayDataSummaryKey) =>
              addGatewayDataToSummary(summaryAggregated, fieldKey, gateway), summary);
        }, summaryTemplate);
      }
    },
  );
