import * as React from 'react';
import {translate} from '../../services/translationService';
import {Clickable, HasContent, OnClick, RenderFunction} from '../../types/Types';
import {ActionMenuItem, ActionMenuItemProps} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

export const SelectionResultActionsDropdown = (props: Clickable & HasContent) => {
  const {onClick: syncAllMetersOnPage} = props;

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const menuItemProps: ActionMenuItemProps = {
      name: translate('sync all meters on this page'),
      onClick: () => {
        onClick();
        syncAllMetersOnPage();
      },
    };
    return ([
      <ActionMenuItem {...menuItemProps} key="sync-meters-menu-item"/>,
    ]);
  };

  return (
    <ActionsDropdown
      renderPopoverContent={renderPopoverContent}
      className="SelectionResultActionDropdown"
    />);
};
