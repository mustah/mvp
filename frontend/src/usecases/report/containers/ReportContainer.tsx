import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {IndicatorWidgets, SelectedIndicatorWidgetProps} from '../../common/components/indicators/IndicatorWidgets';
import {SelectionOverview} from '../../common/components/selection-overview/SelectionOverview';
import {Column} from '../../common/components/layouts/column/Column';
import {Content} from '../../common/components/layouts/content/Content';
import {Layout} from '../../common/components/layouts/layout/Layout';
import {ReportOverview} from '../components/ReportOverview';
import {indicators, ReportState} from '../models/ReportModels';
import {fetchReports} from '../reportActions';
import {selectReportIndicatorWidget} from '../../ui/indicatorActions';

export interface ReportContainerProps extends SelectedIndicatorWidgetProps {
  fetchReports: () => any;
  report: ReportState;
}

const ReportContainer = (props: ReportContainerProps & InjectedAuthRouterProps) => {
  const {selectedWidget, selectIndicatorWidget} = props;
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={translate('all')}/>
        <Content>
          <ReportOverview/>

          <IndicatorWidgets
            indicators={indicators}
            selectedWidget={selectedWidget}
            selectIndicatorWidget={selectIndicatorWidget}
            className="small"
          />

          <Column className="Section">
            <Image src="usecases/report/img/graph-map.png"/>
          </Column>
        </Content>
      </Column>
    </Layout>
  );
};

const mapStateToProps = (state: RootState) => {
  const {report} = state;
  return {
    report,
    selectedWidget: state.ui.indicator.selectedIndicators.report,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchReports,
  selectIndicatorWidget: selectReportIndicatorWidget,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ReportContainer);
