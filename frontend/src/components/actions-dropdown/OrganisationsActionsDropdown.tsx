import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {translate} from '../../services/translationService';
import {OnClick, RenderFunction} from '../../types/Types';
import {ActionMenuItem} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';
import 'ActionsDropdown.scss';

export const OrganisationsActionsDropdown = () => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    return [(
      <Link to={routes.adminOrganisationsAdd} className="link" key={'add organisation'}>
        <ActionMenuItem name={translate('add organisation')} onClick={onClick}/>
      </Link>),
    ];
  };

  return (<ActionsDropdown className="ActionsDropdown-Admin" renderPopoverContent={renderPopoverContent}/>);
};
