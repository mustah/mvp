import {Pie, PieData2} from '../../../components/pie-chart-selector/PieChartSelector2';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {uuid} from '../../../types/Types';

interface DataOverview {
  flagged: PieData2;
  city: PieData2;
  manufacturer: PieData2;
  medium: PieData2;
  status: PieData2;
  alarm: PieData2;
}

type DataOverviewKey = keyof DataOverview;

const entityAdd = (summary: DataOverview, fieldKey: DataOverviewKey, meter: Meter): {[key: string]: Pie} => {
  let label: uuid;
  let existentEntity: Pie | undefined;
  let value: number;

  switch (fieldKey) {

    case 'flagged':
      label = meter[fieldKey] ? 'flagged' : 'unFlagged';
      existentEntity = summary[fieldKey][label];
      value = existentEntity ? ++existentEntity.value : 1;
      return {[label]: {name: label, value, filterParam: meter[fieldKey]}};

    case 'city':
    case 'status':
      label = meter[fieldKey].id ;
      existentEntity = summary[fieldKey][label];
      value = existentEntity ? ++existentEntity.value : 1;
      return {[label]: {name: meter[fieldKey].name, value, filterParam: label}};

    default:
      label = meter[fieldKey];
      existentEntity = summary[fieldKey][label];
      value = existentEntity ? ++existentEntity.value : 1;
      return {[label]: {name: label, value, filterParam: label}};
  }
};

const addMeterDataToSummary = (summary, fieldKey: DataOverviewKey, meter: Meter): DataOverview => {
  return {
    ...summary,
    [fieldKey]: {
      ...summary[fieldKey],
      ...entityAdd(summary, fieldKey, meter),
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
