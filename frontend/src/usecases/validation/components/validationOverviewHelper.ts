import {Pie, PieData2} from '../../../components/pie-chart-selector/PieChartSelector2';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {IdNamed, uuid} from '../../../types/Types';
import {pieChartTranslation} from '../../../services/translationService';
import {FilterParam} from '../../../state/search/selection/selectionModels';

interface DataOverview {
  flagged: PieData2;
  city: PieData2;
  manufacturer: PieData2;
  medium: PieData2;
  status: PieData2;
  alarm: PieData2;
}

export type DataOverviewKey = keyof DataOverview;

const addToCategory = (category: PieData2, fieldKey: DataOverviewKey, meter: Meter): PieData2 => {
  let label: uuid;
  let existentEntity: Pie | undefined;
  let value: number;

  const categoryAdd = (fieldKey: DataOverviewKey, idNamed: IdNamed, filterParam: FilterParam) => ({
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

const addMeterDataToSummary = (summary, fieldKey: DataOverviewKey, meter: Meter): DataOverview => {
  const category: PieData2 = summary[fieldKey];
  return {
    ...summary,
    [fieldKey]: {
      ...addToCategory(category, fieldKey, meter),
    },
  };
};

export const dataSummary = (meters: uuid[], metersLookup: DomainModel<Meter>): DataOverview => {

  const summaryTemplate: {[P in DataOverviewKey]: any} = {
    flagged: {}, city: {}, manufacturer: {}, medium: {}, status: {}, alarm: {},
  };

  return meters.reduce((summary, meterId: uuid) => {
    const meter = metersLookup[meterId];
    return Object.keys(summaryTemplate).reduce(
      (summaryAggregated, fieldKey: DataOverviewKey) =>
        addMeterDataToSummary(summaryAggregated, fieldKey, meter), summary);
  }, summaryTemplate);
};
