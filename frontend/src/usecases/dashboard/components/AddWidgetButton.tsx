import FlatButton from 'material-ui/FlatButton';
import Add from 'material-ui/svg-icons/content/add';
import * as React from 'react';
import {bgHoverColor, border, colors, svgIconProps} from '../../../app/themes';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {IconProps} from '../../../components/popover/PopoverMenu';
import {translate} from '../../../services/translationService';
import {OnClick, RenderFunction} from '../../../types/Types';
import './AddWidgetButton.scss';

interface Props {
  renderPopoverContent: RenderFunction<OnClick>;
}

export const AddWidgetButton = ({renderPopoverContent}: Props) => {
  const iconProps: IconProps = {
    color: svgIconProps.color,
    icon: <Add color={svgIconProps.color}/>,
    hoverColor: bgHoverColor,
    backgroundColor: colors.white,
    label: translate('add new widget'),
    labelStyle: {color: svgIconProps.color},
    style: {
      height: 40,
      borderRadius: 40 / 2,
      border,
      zIndex: 4,
    },
  };

  return (
    <ActionsDropdown
      className="AddWidgetButton"
      Icon={FlatButton}
      iconProps={iconProps}
      renderPopoverContent={renderPopoverContent}
    />
  );
};
