import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {selectReportIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {getSelectedIndicatorReport} from '../../../state/ui/indicator/indicatorSelectors';
import {paperStyle} from '../../app/themes';
import {IndicatorWidgets, SelectedIndicatorWidgetProps} from '../../common/components/indicators/IndicatorWidgets';
import {IndicatorType} from '../../common/components/indicators/models/IndicatorModels';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle} from '../../common/components/texts/Titles';
import {PageContainer} from '../../common/containers/PageContainer';
import {GraphContainer} from '../../graph/GraphContainer';
import {indicators, ReportState} from '../models/ReportModels';
import {fetchReports} from '../reportActions';
import {SummaryContainer} from '../../common/containers/SummaryContainer';
import {PeriodContainer} from '../../common/containers/PeriodContainer';

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
        <Row>
          <SummaryContainer/>
          <PeriodContainer/>
        </Row>
      </Row>

      <IndicatorWidgets
        indicators={indicators}
        selectedWidget={selectedWidget}
        selectIndicatorWidget={selectIndicatorWidget}
        showSelected={true}
      />

      <Paper style={{...paperStyle, marginTop: 24}}>
        <GraphContainer/>
      </Paper>
    </PageContainer>
  );
};

const mapStateToProps = (state: RootState): StateToProps => {
  const {report, ui} = state;
  return {
    report,
    selectedWidget: getSelectedIndicatorReport(ui),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchReports,
  selectIndicatorWidget: selectReportIndicatorWidget,
}, dispatch);

export default connect<StateToProps, DispatchToProps, {}>(mapStateToProps, mapDispatchToProps)(ReportContainer);
