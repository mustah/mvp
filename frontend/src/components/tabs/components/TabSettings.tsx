import * as React from 'react';
import {translate} from '../../../services/translationService';
import {OnClick, RenderFunction} from '../../../types/Types';
import {ActionMenuItem} from '../../actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../actions-dropdown/ActionsDropdown';
import {Column} from '../../layouts/column/Column';
import {TabUnderline} from './TabUnderliner';

export const TabSettings = () => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => [
    {name: translate('export to Excel (.csv)'), onClick},
    {name: translate('export to JSON'), onClick},
  ].map(ActionMenuItem);

  return (
    <Column className="TabSettings">
      <ActionsDropdown renderPopoverContent={renderPopoverContent} className="flex-1 Row-right"/>
      <TabUnderline/>
    </Column>
  );
};
