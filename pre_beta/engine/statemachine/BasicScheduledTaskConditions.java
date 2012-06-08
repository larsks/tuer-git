package engine.statemachine;

public class BasicScheduledTaskConditions {

	private BasicScheduledTaskConditions() {
	}

	public static <S> ScheduledTaskCondition<S> always(){
		return new ScheduledTaskCondition<S>() {
			@Override
			public boolean isSatisfied(final S previousState,final S currentState) {
				return(true);
			}
		};
	}
	
	public static <S> ScheduledTaskCondition<S> and(final ScheduledTaskCondition<S> first,final ScheduledTaskCondition<S> second){
		return new ScheduledTaskCondition<S>() {
			@Override
			public boolean isSatisfied(final S previousState,final S currentState) {
				return(first.isSatisfied(previousState,currentState) && second.isSatisfied(previousState,currentState));
			}
		};
	}
	
	public static <S> ScheduledTaskCondition<S> or(final ScheduledTaskCondition<S> first,final ScheduledTaskCondition<S> second){
		return new ScheduledTaskCondition<S>() {
			@Override
			public boolean isSatisfied(final S previousState,final S currentState) {
				return(first.isSatisfied(previousState,currentState) || second.isSatisfied(previousState,currentState));
			}
		};
	}
	
	public static <S> ScheduledTaskCondition<S> xor(final ScheduledTaskCondition<S> first,final ScheduledTaskCondition<S> second){
		return new ScheduledTaskCondition<S>() {
			@Override
			public boolean isSatisfied(final S previousState,final S currentState) {
				return(first.isSatisfied(previousState,currentState) ^ second.isSatisfied(previousState,currentState));
			}
		};
	}
	
	public static <S> ScheduledTaskCondition<S> not(final ScheduledTaskCondition<S> condition){
		return new ScheduledTaskCondition<S>() {
			@Override
			public boolean isSatisfied(final S previousState,final S currentState) {
				return(!condition.isSatisfied(previousState,currentState));
			}
		};
	}
}