import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {RootState} from '../../../reducers/index';
import {translate} from '../../../services/translationService';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {Image} from '../../common/components/images/Image';
import {PageContainer} from '../../common/components/layouts/layout/PageLayout';
import {Row} from '../../common/components/layouts/row/Row';
import {MainTitle, Title} from '../../common/components/texts/Title';
import {ValidationState} from '../models/Validations';
import {fetchValidations} from '../validationActions';
import ValidationTabsContainer from './ValidationTabsContainer';

export interface ValidationContainerProps {
  fetchValidations: () => any;
  validation: ValidationState;
}

const ValidationContainer = (props: ValidationContainerProps & InjectedAuthRouterProps) => {
  return (
    <PageContainer>
      <Row className="space-between">
        <MainTitle>{translate('validation')}</MainTitle>
        <PeriodSelection/>
      </Row>

      <Image src="usecases/validation/img/alarms.png"/>

      <Title>{translate('meter')}</Title>
      <ValidationTabsContainer/>

    </PageContainer>
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
