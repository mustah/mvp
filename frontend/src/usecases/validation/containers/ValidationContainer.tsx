import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Column} from '../../layouts/components/column/Column';
import {Content} from '../../layouts/components/content/Content';
import {Layout} from '../../layouts/components/layout/Layout';
import {ViewSwitchContainer} from '../../viewSwitch/containers/ViewSwitchContainer';
import {viewSwitchChangeTab} from '../../viewSwitch/viewSwitchActions';
import {TabView} from '../../viewSwitch/viewSwitchReducer';
import {ValidationState} from '../models/Validations';
import {fetchValidations} from '../validationActions';
import {ValidationOverviewContainer} from './ValidationOverviewContainer';

export interface ValidationContainerProps {
  fetchValidations: () => any;
  validation: ValidationState;
  tabView: TabView;
  viewSwitchChangeTab: () => any;
}

const ValidationContainer = (props: ValidationContainerProps & InjectedAuthRouterProps) => {
  const {fetchValidations, tabView, viewSwitchChangeTab} = props;
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={'Allt'}/>
        <Content>
          <ValidationOverviewContainer/>
          <div className="button" onClick={fetchValidations}>VALIDATIONS</div>
          <ViewSwitchContainer useCase="validation" tabView={tabView} viewSwitchChangeTab={viewSwitchChangeTab}/>
        </Content>
      </Column>
    </Layout>
  );
};

const mapStateToProps = (state: RootState) => {
  const {validation, viewSwitch} = state;
  return {
    validation,
    tabView: viewSwitch.validation,
}
  ;
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchValidations,
  viewSwitchChangeTab,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationContainer);
