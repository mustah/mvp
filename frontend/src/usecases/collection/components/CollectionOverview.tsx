import * as React from 'react';
import {Row} from '../../../components/layouts/row/Row';
import {PieChartSelector, PieChartSelectorProps} from '../../../components/pie-chart-selector/PieChartSelector';
import {translate} from '../../../services/translationService';
import {GatewayDataSummary} from '../../../state/domain-models/gateway/gatewayModels';
import {ParameterName, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {uuid} from '../../../types/Types';
import './CollectionOverview.scss';

interface CollectionOverviewProps {
  gatewayDataSummary: GatewayDataSummary | null;
  addSelection: (searchParameters: SelectionParameter) => void;
}

const colors: string[][] = [
  ['#e8a090', '#fce8cc'],
  ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
  ['#b7e000', '#f7be29', '#ed4200'],
];

export const CollectionOverview = (props: CollectionOverviewProps) => {

  const {gatewayDataSummary, addSelection} = props;

  const selectStatus = (id: uuid) => addSelection({parameter: ParameterName.gatewayStatuses, id});
  const selectCity = (id: uuid) => addSelection({parameter: ParameterName.cities, id});
  const selectProductModel = (id: uuid) => addSelection({parameter: ParameterName.productModels, id});

  if (!gatewayDataSummary) {
    return null;
  } else {
    const pieCharts: PieChartSelectorProps[] = [
      {
        heading: translate('status'),
        data: gatewayDataSummary.status,
        colors: colors[0],
        onClick: selectStatus,
        maxSlices: 4,
      },
      {
        heading: translate('flagged for action'),
        data: gatewayDataSummary.flagged,
        colors: colors[1],
        maxSlices: 4,
      },
      {
        heading: translate('cities'),
        data: gatewayDataSummary.city,
        colors: colors[0],
        onClick: selectCity,
        maxSlices: 4,
      },
      {
        heading: translate('product models'),
        data: gatewayDataSummary.productModel,
        colors: colors[1],
        onClick: selectProductModel,
        maxSlices: 4,
      },
    ];
    return (
      <Row className="CollectionOverview">
        {pieCharts.map((pieChart: PieChartSelectorProps, index) => <PieChartSelector key={index} {...pieChart}/>)}
      </Row>
    );
  }
};
