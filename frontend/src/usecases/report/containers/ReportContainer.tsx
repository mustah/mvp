import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {Image} from '../../common/components/images/Image';
import {IndicatorWidgets, SelectedIndicatorWidgetProps} from '../../common/components/indicators/IndicatorWidgets';
import {IndicatorType} from '../../common/components/indicators/models/IndicatorModels';
import {Column} from '../../common/components/layouts/column/Column';
import {PageContainer} from '../../common/components/layouts/layout/PageLayout';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle} from '../../common/components/texts/Title';
import {selectReportIndicatorWidget} from '../../../state/ui/indicatorActions';
import {indicators, ReportState} from '../models/ReportModels';
import {fetchReports} from '../reportActions';

interface StateToProps extends SelectedIndicatorWidgetProps {
  report: ReportState;
}

interface DispatchToProps {
  fetchReports: () => any;
  selectIndicatorWidget: (type: IndicatorType) => any;
}

const ReportContainer = (props: StateToProps & DispatchToProps & InjectedAuthRouterProps) => {
  const {selectedWidget, selectIndicatorWidget} = props;
  return (
    <PageContainer>
      <Row className="space-between">
        <MainTitle>{translate('report')}</MainTitle>
        <PeriodSelection/>
      </Row>

      <IndicatorWidgets
        indicators={indicators}
        selectedWidget={selectedWidget}
        selectIndicatorWidget={selectIndicatorWidget}
        className="small"
      />

      <Column className="Section">
        <Image src="usecases/report/img/graph-map.png"/>
      </Column>
    </PageContainer>
  );
};

const mapStateToProps = (state: RootState): StateToProps => {
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

export default connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(ReportContainer);
