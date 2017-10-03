import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {Image} from '../../common/components/images/Image';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Title} from '../../common/components/texts/Title';
import {Column} from '../../layouts/components/column/Column';
import {Content} from '../../layouts/components/content/Content';
import {Layout} from '../../layouts/components/layout/Layout';
import {ValidationOverview} from '../components/ValidationOverview';
import {ValidationState} from '../models/Validations';
import {fetchValidations} from '../validationActions';
import ValidationTabsContainer from './ValidationTabsContainer';

export interface ValidationContainerProps {
  fetchValidations: () => any;
  validation: ValidationState;
}

const ValidationContainer = (props: ValidationContainerProps & InjectedAuthRouterProps) => {
  const {fetchValidations} = props;
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={'Allt'}/>
        <Content>
          <ValidationOverview/>
          <Image src="usecases/validation/img/alarms.png"/>

          <Title>{translate('meters')}</Title>
          <Image src="usecases/validation/img/meters.png"/>

          <div className="button" onClick={fetchValidations}>VALIDATIONS</div>
          <ValidationTabsContainer />
        </Content>
      </Column>
    </Layout>
  );
};

const mapStateToProps = (state: RootState) => {
  const {validation} = state;
  return {
    validation,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchValidations,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationContainer);
