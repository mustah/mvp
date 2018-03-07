import {createSelector} from 'reselect';
import {PieData, PieSlice} from '../../../components/pie-chart-selector/PieChartSelector';
import {hasItems} from '../../../helpers/functionalHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {pieChartTranslation} from '../../../helpers/translations';
import {IdNamed, uuid} from '../../../types/Types';
import {FilterParam} from '../../search/selection/selectionModels';
import {ObjectsById} from '../domainModels';
import {getEntitiesDomainModels, getResultDomainModels} from '../domainModelsSelectors';
import {Gateway, GatewayDataSummary, GatewayDataSummaryKey, GatewaysState} from './gatewayModels';

const addToPie = (category: PieData, fieldKey: GatewayDataSummaryKey, gateway: Gateway): PieData => {

  const sliceUpdate =
    (fieldKey: GatewayDataSummaryKey, idNamed: IdNamed, filterParam: FilterParam, value: number): PieSlice => ({
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

  switch (fieldKey) {
    case 'flagged':
      label = gateway[fieldKey] ? 'flagged' : 'unFlagged';
      return {
        ...category,
        [label]: sliceUpdate(fieldKey, {
          id: label,
          name: label,
        }, gateway[fieldKey], initOrIncrease(category[label])),
      };
    case 'location':
      label = gateway[fieldKey].city.id;
      return {
        ...category,
        [label]: sliceUpdate(fieldKey, gateway[fieldKey].city, label, initOrIncrease(category[label])),
      };
    case 'status':
      label = gateway[fieldKey].id;
      return {
        ...category,
        [label]: sliceUpdate(fieldKey, gateway[fieldKey], label, initOrIncrease(category[label])),
      };
    default:
      label = gateway[fieldKey];
      return {
        ...category,
        [label]: sliceUpdate(fieldKey, {
          id: label,
          name: label as string,
        }, label, initOrIncrease(category[label])),
      };
  }
};

const createDataSummary = (summary, fieldKey: GatewayDataSummaryKey, gateway: Gateway): GatewayDataSummary => {
  const category: PieData = summary[fieldKey];
  return {
    ...summary,
    [fieldKey]: {
      ...addToPie(category, fieldKey, gateway),
    },
  };
};

// TODO: "addToPie" and "createDataSummary" pretty much duplicates of the same functions for getMeterDataSummary
// consider to refactor and remove duplicated code.
export const getGatewayDataSummary =
  createSelector<GatewaysState, uuid[], ObjectsById<Gateway>, Maybe<GatewayDataSummary>>(
    getResultDomainModels,
    getEntitiesDomainModels,
    (gatewayIds: uuid[], gateways: ObjectsById<Gateway>): Maybe<GatewayDataSummary> => {
      const summaryTemplate: {[P in GatewayDataSummaryKey]: PieData} = {
        status: {}, flagged: {}, location: {}, productModel: {},
      };

      return Maybe.just(gatewayIds)
        .filter(hasItems)
        .flatMap((ids: uuid[]) => Maybe.just<GatewayDataSummary>(
          ids.reduce((summary, gatewayId: uuid) => {
            const gateway = gateways[gatewayId];
            return Object.keys(summaryTemplate).reduce(
              (summaryAggregated, fieldKey: GatewayDataSummaryKey) =>
                createDataSummary(summaryAggregated, fieldKey, gateway), summary);
          }, summaryTemplate)),
        );
    },
  );
