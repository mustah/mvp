import * as React from 'react';
import 'ValidationOverview.scss';
import {Row} from '../../../components/layouts/row/Row';
import {PieChartSelector} from '../../../components/pie-chart-selector/PieChartSelector2';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {ParameterName, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {uuid} from '../../../types/Types';
import {dataSummary} from './validationOverviewHelper';

// TODO: Perhaps move this to themes and make customizable.
const colors: string[][] = [
  ['#E91E63', '#fce8cc', '#3F51B5', '#2196F3', '#009688'],
  ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
  ['#b7e000', '#f7be29', '#ed4200'],
];

interface ValidationOverviewProps {
  metersLookup: DomainModel<Meter>;
  meters: uuid[];
  addSelection: (searchParameters: SelectionParameter) => void;
}

export const ValidationOverview = (props: ValidationOverviewProps) => {
  const {
    addSelection,
    meters,
    metersLookup,
  } = props;

  const selectStatus = (id: uuid) => addSelection({parameter: ParameterName.meterStatuses, id});
  const selectCity = (id: uuid) => addSelection({parameter: ParameterName.cities, id});
  const selectManufacturer = (id: uuid) => addSelection({parameter: ParameterName.manufacturers, id});
  const selectAlarm = (id: uuid) => addSelection({parameter: ParameterName.alarms, id});

  const PieChartData = dataSummary(meters, metersLookup);

// TODO: handle case when there are zero meters.
  return (
    <Row className="ValidationOverview">
      <PieChartSelector
        heading={translate('status')}
        data={PieChartData.status}
        colors={colors[0]}
        onClick={selectStatus}
        maxSlices={4}
      />
      <PieChartSelector
        heading={translate('flagged for action')}
        data={PieChartData.flagged}
        colors={colors[1]}
        maxSlices={4}
      />
      <PieChartSelector
        heading={translate('alarm', {count: Object.keys(PieChartData.alarm).length})}
        data={PieChartData.alarm}
        colors={colors[0]}
        onClick={selectAlarm}
        maxSlices={4}
      />
      <PieChartSelector
        heading={translate('cities')}
        data={PieChartData.city}
        colors={colors[1]}
        onClick={selectCity}
        maxSlices={4}
      />
      <PieChartSelector
        heading={translate('manufacturer')}
        data={PieChartData.manufacturer}
        colors={colors[0]}
        onClick={selectManufacturer}
        maxSlices={4}
      />
      <PieChartSelector
        heading={translate('medium')}
        data={PieChartData.medium}
        colors={colors[1]}
        maxSlices={4}
      />
    </Row>
  );
};
