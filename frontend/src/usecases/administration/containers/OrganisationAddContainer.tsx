import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {OrganisationEditForm} from '../../../components/forms/OrganisationEditForm';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {PageTitle} from '../../../components/texts/Titles';
import {AdminPageComponent} from '../../../containers/PageComponent';
import {translate} from '../../../services/translationService';
import {addOrganisation} from '../../../state/domain-models/organisation/organisationsApiActions';
import {OnClick} from '../../../types/Types';

interface DispatchToProps {
  addOrganisation: OnClick;
}

type OwnProps = InjectedAuthRouterProps;
type Props = OwnProps & DispatchToProps;

const OrganisationAdd = ({addOrganisation}: Props) => (
  <AdminPageComponent>
    <PageTitle>
      {translate('add organisation')}
    </PageTitle>

    <Paper style={paperStyle}>
      <WrapperIndent>
        <OrganisationEditForm onSubmit={addOrganisation}/>
      </WrapperIndent>
    </Paper>
  </AdminPageComponent>
);

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addOrganisation,
}, dispatch);

export const OrganisationAddContainer =
  connect<null, DispatchToProps, OwnProps>(null, mapDispatchToProps)(OrganisationAdd);
