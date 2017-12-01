import * as React from 'react';
import {translate} from '../../../services/translationService';
import {PieChartSelector} from '../../../components/pie-chart-selector/PieChartSelector2';
import {Row} from '../../../components/layouts/row/Row';
import {uuid} from '../../../types/Types';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {dataSummary} from './overviewDataHelper';

// TODO: Perhaps move this to themes and make customizable.
const colors: [string[]] = [
  ['#E91E63', '#fce8cc', '#3F51B5', '#2196F3', '#009688'],
  ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
  ['#b7e000', '#f7be29', '#ed4200'],
];

// TODO: Add correct types
interface OverviewProps {
  selectStatus: any;
  selectCity: any;
  selectManufacturer: any;
  meters: uuid[];
  metersLookup: DomainModel<Meter>;

}

// TODO: Perhaps make dynamic, to make it more reusable. Have an array of {heading, data, colors, onClick}-objects
// as an input.
export const Overview = (props: OverviewProps) => {
  const {selectStatus, selectManufacturer, selectCity, meters, metersLookup} = props;
  const PieChartData = dataSummary(meters, metersLookup);
  return (
    <Row>
      <PieChartSelector
        heading={translate('status')}
        data={PieChartData.status}
        colors={colors[0]}
        onClick={selectStatus}
        maxLegends={4}
      />
      <PieChartSelector
        heading={translate('flagged for action')}
        data={PieChartData.flagged}
        colors={colors[1]}
        maxLegends={4}
      />
      <PieChartSelector
        heading={translate('alarm', {count: Object.keys(PieChartData.alarm).length})}
        data={PieChartData.alarm}
        colors={colors[0]}
        maxLegends={4}
      />
      <PieChartSelector
        heading={translate('cities')}
        data={PieChartData.city}
        colors={colors[1]}
        onClick={selectCity}
        maxLegends={4}
      />
      <PieChartSelector
        heading={translate('manufacturer')}
        data={PieChartData.manufacturer}
        colors={colors[0]}
        onClick={selectManufacturer}
        maxLegends={4}
      />
      <PieChartSelector
        heading={translate('medium')}
        data={PieChartData.medium}
        colors={colors[1]}
        maxLegends={4}
      />
    </Row>);
};
