import * as React from 'react';
import {OnClick, RenderFunction} from '../../types/Types';
import {IconMore} from '../icons/IconMore';
import {IconProps, PopoverMenu} from '../popover/PopoverMenu';

interface Props {
  renderPopoverContent: RenderFunction<OnClick>;
  className?: string;
  icon?: any;
  iconProps?: IconProps;
}

export const ActionsDropdown = ({renderPopoverContent, className, icon, iconProps}: Props) => (
  <PopoverMenu
    className={className}
    IconComponent={icon || IconMore}
    iconProps={iconProps}
    renderPopoverContent={renderPopoverContent}
  />
);
