import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {translate} from '../../services/translationService';
import {OnClick, RenderFunction} from '../../types/Types';
import {ActionMenuItem} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';
import 'UsersActionsDropdown.scss';

export const UsersActionsDropdown = () => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    return [(
      <Link to={routes.adminUsersAdd} className="link" key={'add user'}>
        <ActionMenuItem name={translate('add user')} onClick={onClick}/>
      </Link>),
    ];
  };

  return (<ActionsDropdown className="UsersActionsDropdown" renderPopoverContent={renderPopoverContent}/>);
};
