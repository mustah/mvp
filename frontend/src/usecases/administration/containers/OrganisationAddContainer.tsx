import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {OrganisationEditForm} from '../../../components/forms/OrganisationEditForm';
import {Row} from '../../../components/layouts/row/Row';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {translate} from '../../../services/translationService';
import {addOrganisation} from '../../../state/domain-models/organisation/organisationsApiActions';
import {OnClick} from '../../../types/Types';

interface DispatchToProps {
  addOrganisation: OnClick;
}

type OwnProps = InjectedAuthRouterProps;
type Props = OwnProps & DispatchToProps;

const OrganisationAdd = ({addOrganisation}: Props) => {
  return (
    <PageComponent isSideMenuOpen={false}>
      <Row className="space-between">
        <MainTitle>
          {translate('add organisation')}
        </MainTitle>
      </Row>

      <Paper style={paperStyle}>
        <WrapperIndent>
          <OrganisationEditForm onSubmit={addOrganisation}/>
        </WrapperIndent>
      </Paper>
    </PageComponent>
  );
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addOrganisation,
}, dispatch);

export const OrganisationAddContainer =
  connect<null, DispatchToProps, OwnProps>(null, mapDispatchToProps)(OrganisationAdd);
