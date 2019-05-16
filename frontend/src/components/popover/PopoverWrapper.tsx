import {default as classNames} from 'classnames';
import Popover from 'material-ui/Popover/Popover';
import * as React from 'react';
import {popoverStyle} from '../../app/themes';
import {ClassNamed, OnClick, RenderFunction, WithChildren} from '../../types/Types';
import {Row} from '../layouts/row/Row';
import './PopoverMenu.scss';
import origin = __MaterialUI.propTypes.origin;

interface Props extends ClassNamed, WithChildren {
  renderPopoverContent: RenderFunction<OnClick>;
  onRequestClose?: OnClick;
  anchorOrigin?: origin;
  targetOrigin?: origin;
}

interface State {
  isOpen: boolean;
  anchorElement?: React.ReactInstance;
}

const anchorOrigin: origin = {horizontal: 'right', vertical: 'bottom'};
const targetOrigin: origin = {horizontal: 'right', vertical: 'top'};

export const PopoverWrapper = ({className, children, onRequestClose, renderPopoverContent}: Props) => {
  const [state, setState] = React.useState<State>({isOpen: false});

  const onClose = () => {
    setState({isOpen: false});
    if (onRequestClose) {
      onRequestClose();
    }
  };

  const openPopover = (event: any): void => {
    event.preventDefault();
    setState({
      isOpen: true,
      anchorElement: event.currentTarget,
    });
  };

  return (
    <Row className={className} onClick={openPopover}>
      {children}
      <Popover
        className={classNames('PopoverMenu-Component', className)}
        open={state.isOpen}
        anchorEl={state.anchorElement}
        anchorOrigin={anchorOrigin}
        targetOrigin={targetOrigin}
        onRequestClose={onClose}
        style={popoverStyle}
      >
        <div>
          {renderPopoverContent(onClose)}
        </div>
      </Popover>
    </Row>
  );
};
