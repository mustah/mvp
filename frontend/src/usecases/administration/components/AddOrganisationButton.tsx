import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import '../../../components/actions-dropdown/ActionsDropdown.scss';
import {ButtonAdd} from '../../../components/buttons/ButtonAdd';
import {translate} from '../../../services/translationService';

export const AddOrganisationButton = () => (
  <Link to={routes.adminOrganisationsAdd} className="link" key={'add organisation'}>
    <ButtonAdd label={translate('add organisation')}/>
  </Link>
);
