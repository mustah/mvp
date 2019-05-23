import * as React from 'react';
import {routes} from '../../../app/routes';
import '../../../components/actions-dropdown/ActionsDropdown.scss';
import {ButtonAdd} from '../../../components/buttons/ButtonAdd';
import {Link} from '../../../components/links/Link';
import {translate} from '../../../services/translationService';

export const AddOrganisationButton = () => (
  <Link to={routes.adminOrganisationsAdd} key={'add organisation'}>
    <ButtonAdd label={translate('add organisation')}/>
  </Link>
);
