import {default as classNames} from 'classnames';
import * as React from 'react';
import {Callback, ClassNamed, WithChildren} from '../../../types/Types';
import {IconRightArrow} from '../../icons/IconRightArrow';
import {BoldFirstUpper} from '../../texts/Texts';
import {Column} from '../column/Column';
import {RowMiddle} from '../row/Row';
import './Foldable.scss';

interface Props extends ClassNamed, WithChildren {
  title: string;
}

interface ToggleVisibilityProps {
  isVisible: boolean;
  showHide: Callback;
}

const useToggleVisibility = (initialState: boolean): ToggleVisibilityProps => {
  const [isVisible, toggle] = React.useState(initialState);
  const showHide = () => toggle(!isVisible);
  return {isVisible, showHide};
};

export const Foldable = ({children, className, title}: Props) => {
  const {isVisible, showHide} = useToggleVisibility(true);

  return (
    <Column className="Foldable">
      <RowMiddle className="Foldable-title">
        <IconRightArrow onClick={showHide} className={classNames('Foldable-arrow', {isVisible})}/>
        <BoldFirstUpper className="Medium">{title}</BoldFirstUpper>
      </RowMiddle>
      <div className={classNames('Foldable-content', className, {isVisible})}>
        {children}
      </div>
    </Column>
  );
};
