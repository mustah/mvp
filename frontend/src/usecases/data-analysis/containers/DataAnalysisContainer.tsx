import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {IndicatorWidgets, SelectedIndicatorWidgetProps} from '../../common/components/indicators/IndicatorWidgets';
import {Indicator, IndicatorType} from '../../common/components/indicators/models/IndicatorModels';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Column} from '../../layouts/components/column/Column';
import {Content} from '../../layouts/components/content/Content';
import {Layout} from '../../layouts/components/layout/Layout';
import {selectReportIndicatorWidget} from '../../ui/uiActions';
import {fetchDataAnalysis} from '../dataAnalysisActions';
import {DataAnalysisState} from '../models/DataAnalysis';
import {DataAnalysisOverviewContainer} from './DataAnalysisOverviewContainer';

export interface DataAnalysisContainerProps extends SelectedIndicatorWidgetProps {
  fetchDataAnalysis: () => any;
  dataAnalysis: DataAnalysisState;
}

const indicators: Indicator[] = [
  {
    type: IndicatorType.current,
    title: 'El',
    state: 'ok',
    value: '123',
    unit: 'kWh/m2',
    subtitle: '(+5)',
  },
  {
    type: IndicatorType.coldWater,
    title: 'Kallvatten',
    state: 'warning',
    value: '53',
    unit: 'l/m2',
    subtitle: '(+6)',
  },
  {
    type: IndicatorType.warmWater,
    title: 'Varmvatten',
    state: 'warning',
    value: '13',
    unit: 'l/m2',
    subtitle: '(-2)',
  },
  {
    type: IndicatorType.districtHeating,
    title: 'Fjärrvärme',
    state: 'ok',
    value: '1.1',
    unit: 'kWh/m2',
    subtitle: '(-2)',
  },
  {
    type: IndicatorType.temperatureInside,
    title: 'Temp Inomhus',
    state: 'ok',
    value: '22.4',
    unit: '°C',
    subtitle: '(+0.2)',
  },
  {
    type: IndicatorType.temperatureOutside,
    title: 'Temp Utomhus',
    state: 'info',
    value: '13',
    unit: '°C',
    subtitle: '(+2)',
  },
];

const DataAnalysisContainer = (props: DataAnalysisContainerProps & InjectedAuthRouterProps) => {
  const {fetchDataAnalysis, selectedWidget, selectIndicatorWidget} = props;
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={'Allt'}/>
        <Content>
          <DataAnalysisOverviewContainer/>
          <MainTitle title={translate('consumption')}/>

          <IndicatorWidgets
            indicators={indicators}
            selectedWidget={selectedWidget}
            selectIndicatorWidget={selectIndicatorWidget}
            className="small"
          />

          <Column className="Section">
            <Image src="usecases/data-analysis/img/graph-map.png"/>
          </Column>

          <div className="button" onClick={fetchDataAnalysis}>DATA_ANALYSIS</div>
        </Content>
      </Column>
    </Layout>
  );
};

const mapStateToProps = (state: RootState) => {
  const {dataAnalysis} = state;
  return {
    dataAnalysis,
    selectedWidget: state.ui.selectedIndicators.report,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchDataAnalysis,
  selectIndicatorWidget: selectReportIndicatorWidget,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(DataAnalysisContainer);
