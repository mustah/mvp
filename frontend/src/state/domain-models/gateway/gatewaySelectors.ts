import {createSelector} from 'reselect';
import {PieData, PieSlice} from '../../../components/pie-chart-selector/PieChartSelector';
import {hasItems} from '../../../helpers/functionalHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {IdNamed, uuid} from '../../../types/Types';
import {pieChartTranslation} from '../../../helpers/translations';
import {FilterParam} from '../../search/selection/selectionModels';
import {DomainModel} from '../domainModels';
import {getResultDomainModels} from '../domainModelsSelectors';
import {Gateway, GatewayDataSummary, GatewayDataSummaryKey, GatewaysState} from './gatewayModels';

export const getGatewaysTotal = (state: GatewaysState): number => state.total;
export const getGatewayEntities = (state: GatewaysState): DomainModel<Gateway> => state.entities;

const addToCategory = (category: PieData, fieldKey: GatewayDataSummaryKey, gateway: Gateway): PieData => {

  const categoryAdd =
    (fieldKey: GatewayDataSummaryKey, idNamed: IdNamed, filterParam: FilterParam, value: number): PieSlice => ({
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
      label = gateway[fieldKey] ? 'flagged' : 'unFlagged';
      return {
        ...category,
        [label]: categoryAdd(fieldKey, {
          id: label,
          name: label,
        }, gateway[fieldKey], valueOf(category[label])),
      };
    case 'city':
    case 'status':
      label = gateway[fieldKey].id;
      return {
        ...category,
        [label]: categoryAdd(fieldKey, gateway[fieldKey], label, valueOf(category[label])),
      };

    default:
      label = gateway[fieldKey];
      return {
        ...category,
        [label]: categoryAdd(fieldKey, {
          id: label,
          name: label as string,
        }, label, valueOf(category[label])),
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
  createSelector<GatewaysState, uuid[], DomainModel<Gateway>, Maybe<GatewayDataSummary>>(
    getResultDomainModels,
    getGatewayEntities,
    (gatewayIds: uuid[], gateways: DomainModel<Gateway>): Maybe<GatewayDataSummary> => {
      const summaryTemplate: {[P in GatewayDataSummaryKey]: PieData} = {
        status: {}, flagged: {}, city: {}, productModel: {},
      };

      return Maybe.just(gatewayIds)
        .filter(hasItems)
        .flatMap(() => Maybe.just(gatewayIds.reduce((summary, gatewayId: uuid) => {
          const gateway = gateways[gatewayId];
          return Object.keys(summaryTemplate).reduce(
            (summaryAggregated, fieldKey: GatewayDataSummaryKey) =>
              addGatewayDataToSummary(summaryAggregated, fieldKey, gateway), summary);
        }, summaryTemplate)));
    },
  );
