import {Pie, PieData} from '../PieChartSelector';
import {splitDataIntoSlices} from '../pieChartHelper';
import {uuid} from '../../../types/Types';

describe('pieChartHelper', () => {
  const testData: PieData = {
    sto: {name: 'Stockholm', value: 3, filterParam: 'sto'},
    got: {name: 'Göteborg', value: 2, filterParam: 'got'},
    kba: {name: 'Kungsbacka', value: 4, filterParam: 'kba'},
    bor: {name: 'Borås', value: 100, filterParam: 'bor'},
    fri: {name: 'Fristad', value: 7, filterParam: 'fri'},
  };
  const segments: uuid[] = ['sto', 'got', 'kba', 'bor', 'fri'];

  it('splitDataIntoSlices, order input', () => {

    const pieSlices: Pie[] = splitDataIntoSlices(segments, testData, 10);

    expect(pieSlices).toEqual([
      {name: 'Borås', value: 100, filterParam: 'bor'},
      {name: 'Fristad', value: 7, filterParam: 'fri'},
      {name: 'Kungsbacka', value: 4, filterParam: 'kba'},
      {name: 'Stockholm', value: 3, filterParam: 'sto'},
      {name: 'Göteborg', value: 2, filterParam: 'got'},
    ]);
  });

  it('splitDataIntoSlices, group input', () => {
    const pieSlices: Pie[] = splitDataIntoSlices(segments, testData, 3);

    expect(pieSlices).toEqual([
      {name: 'Borås', value: 100, filterParam: 'bor'},
      {name: 'Fristad', value: 7, filterParam: 'fri'},
      {name: 'other', value: 9, filterParam: ['kba', 'sto', 'got']},
    ]);
  });
});
