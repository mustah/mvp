import * as React from 'react';
import {popoverStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {ResolutionAware} from '../../state/report/reportModels';
import {CallbackWith} from '../../types/Types';
import {DropdownMenu, MenuItemProps} from '../dropdown-selector/DropdownMenu';
import {IconTemporalResolution} from '../icons/IconTemporalResolution';
import {TemporalResolution} from './dateModels';

export interface ResolutionProps extends ResolutionAware {
  disabled?: boolean;
  selectResolution: CallbackWith<TemporalResolution>;
}

const width = 124;
const style: React.CSSProperties = {marginRight: 16, marginBottom: 0, marginLeft: 16, width};
const labelStyle: React.CSSProperties = {width};
const listStyle: React.CSSProperties = {width, ...popoverStyle};

export const ResolutionSelection = ({disabled, resolution, selectResolution}: ResolutionProps) => {

  const resolutions: MenuItemProps[] = [
    {
      value: TemporalResolution.all,
      label: firstUpperTranslated('all'),
      primaryText: firstUpperTranslated('all'),
      onClick: () => selectResolution(TemporalResolution.all),
    },
    {
      value: TemporalResolution.hour,
      label: firstUpperTranslated('hour'),
      primaryText: firstUpperTranslated('hour'),
      onClick: () => selectResolution(TemporalResolution.hour),
    },
    {
      value: TemporalResolution.day,
      label: firstUpperTranslated('day'),
      primaryText: firstUpperTranslated('day'),
      onClick: () => selectResolution(TemporalResolution.day),
    },
    {
      value: TemporalResolution.month,
      label: firstUpperTranslated('month'),
      primaryText: firstUpperTranslated('month'),
      onClick: () => selectResolution(TemporalResolution.month),
    },
  ];

  return (
    <DropdownMenu
      disabled={disabled}
      IconButton={IconTemporalResolution}
      labelStyle={labelStyle}
      listStyle={listStyle}
      menuItems={resolutions}
      style={style}
      value={resolution}
    />
  );
};
