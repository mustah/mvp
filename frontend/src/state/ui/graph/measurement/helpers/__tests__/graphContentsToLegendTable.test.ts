import {GraphContents, LegendItem} from '../../../../../../usecases/report/reportModels';
import {Normalized} from '../../../../../domain-models/domainModels';
import {AverageApiResponse, MeasurementResponses, Quantity} from '../../measurementModels';
import {mapApiResponseToGraphData} from '../apiResponseToGraphContents';
import {graphContentsToLegendTable} from '../graphContentsToLegendTable';

describe('graphContentsToLegendTable', () => {

  const emptyResponses = (): MeasurementResponses => ({
    measurement: [],
    average: [],
    cities: [],
  });

  it('hides averages because those are added/removed depending on selected meters', () => {
    const average: AverageApiResponse = [
      {
        id: 'average-Energy',
        quantity: 'Energy' as Quantity,
        unit: 'kWh',
        label: 'average',
        values: [
          {when: 1536710400.000000000, value: -237.99453082035262},
          {when: 1536796800.000000000, value: 325.6393322766128},
          {when: 1536883200.000000000, value: 572.1098490987868},
          {when: 1536969600.000000000, value: -996.959491447525},
          {when: 1537056000.000000000, value: 416.4073993950333},
          {when: 1537142400.000000000},
        ],
      },
      {
        id: 'average-Volume',
        quantity: 'Volume' as Quantity,
        unit: 'm³',
        label: 'average',
        values: [
          {when: 1536710400.000000000, value: -0.9909383115997471},
          {when: 1536796800.000000000, value: 0.6205155387913904},
          {when: 1536883200.000000000, value: -0.23211917419239092},
          {when: 1536969600.000000000, value: -1.1585748185396183},
          {when: 1537056000.000000000, value: 0.40822685298912686},
          {when: 1537142400.000000000},
        ],
      },
    ];

    const graphContents: GraphContents = mapApiResponseToGraphData({
      ...emptyResponses(),
      average,
    });

    expect(graphContents.lines).toHaveLength(2);

    const {result, entities}: Normalized<LegendItem> = graphContentsToLegendTable(graphContents);
    expect(result).toHaveLength(0);
    expect(entities).toEqual({});
  });

});
