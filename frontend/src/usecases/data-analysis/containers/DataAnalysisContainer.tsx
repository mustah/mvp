import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {IndicatorWidgets, SelectedIndicatorWidgetProps} from '../../common/components/indicators/IndicatorWidgets';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Title} from '../../common/components/texts/Title';
import {Column} from '../../layouts/components/column/Column';
import {Content} from '../../layouts/components/content/Content';
import {Layout} from '../../layouts/components/layout/Layout';
import {selectReportIndicatorWidget} from '../../ui/uiActions';
import {fetchDataAnalysis} from '../dataAnalysisActions';
import {DataAnalysisState, indicators} from '../models/DataAnalysis';
import {DataAnalysisOverviewContainer} from './DataAnalysisOverviewContainer';

export interface DataAnalysisContainerProps extends SelectedIndicatorWidgetProps {
  fetchDataAnalysis: () => any;
  dataAnalysis: DataAnalysisState;
}

const DataAnalysisContainer = (props: DataAnalysisContainerProps & InjectedAuthRouterProps) => {
  const {fetchDataAnalysis, selectedWidget, selectIndicatorWidget} = props;
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={translate('all')}/>
        <Content>
          <DataAnalysisOverviewContainer/>
          <Title>{translate('consumption')}</Title>

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
