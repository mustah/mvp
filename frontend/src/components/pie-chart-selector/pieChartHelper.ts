import {Pie, PieData2} from './PieChartSelector2';
import {uuid} from '../../types/Types';
import {translate} from '../../services/translationService';

const sortPieData = (data: Pie[]): Pie[] => {
  const sortFunction = ({value: value1}: Pie, {value: value2}: Pie) =>
    (value1 < value2 ? 1 : value1 > value2 ? -1 : 0);
  return data.sort(sortFunction);
};

const bundleToOther = (data: Pie[]): Pie => {
  const bundle = (prev, curr: Pie): Pie =>
    ({...prev, value: prev.value + curr.value, filterParam: [...prev.filterParam, curr.filterParam]});
  const initBundle: Pie = {
    name: translate('other'),
    value: 0,
    filterParam: [],
  };
  return data.reduce(bundle, initBundle);
};

export const pieData = (fields: uuid[], data: PieData2, maxLegends: number): Pie[] => {

  const pieSlices = fields.map((field) => (data[field]));
  const pieSlicesSorted = sortPieData(pieSlices);

  if (fields.length >= maxLegends) {
    const largestFields = pieSlicesSorted.slice(0, maxLegends - 1);
    const other = pieSlicesSorted.slice(maxLegends - 1);
    return [...largestFields, bundleToOther(other)];
  } else {
    return pieSlices;
  }
};
