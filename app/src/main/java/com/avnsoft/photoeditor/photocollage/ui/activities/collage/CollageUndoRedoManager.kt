package com.avnsoft.photoeditor.photocollage.ui.activities.collage

import com.avnsoft.photoeditor.photocollage.data.model.collage.CollageState


/**
 * Manager để quản lý undo/redo stack cho collage editor
 */
class CollageUndoRedoManager(
    private val maxStackSize: Int = 50
) {
    private val undoStack = ArrayDeque<CollageState>(maxStackSize)
    private val redoStack = ArrayDeque<CollageState>(maxStackSize)

    /**
     * Lưu state vào redo stack (khi confirm)
     * Chỉ lưu nếu state khác với state cuối cùng trong redo stack (tránh duplicate)
     * Không xóa undo stack
     */
    fun saveState(state: CollageState) {
        // Kiểm tra xem state có giống với state cuối cùng trong redo stack không
        val lastState = redoStack.lastOrNull()
        if (lastState != null && statesAreEqual(lastState, state)) {
            // Không lưu nếu giống nhau (tránh duplicate)
            return
        }

        // Lưu state vào redo stack
        if (redoStack.size >= maxStackSize) {
            redoStack.removeFirst()
        }
        redoStack.addLast(state.copy())
    }

    /**
     * So sánh 2 state xem có giống nhau không
     * Sử dụng equals() của data class để so sánh (bao gồm cả lists)
     */
    private fun statesAreEqual(state1: CollageState, state2: CollageState): Boolean {
        // Data class tự động implement equals(), nhưng để rõ ràng, ta so sánh từng field
        // Hoặc có thể dùng trực tiếp: state1 == state2
        return state1.templateId == state2.templateId &&
                state1.topMargin == state2.topMargin &&
                state1.columnMargin == state2.columnMargin &&
                state1.cornerRadius == state2.cornerRadius &&
                state1.ratio == state2.ratio &&
                state1.backgroundSelection == state2.backgroundSelection &&
                state1.frameSelection == state2.frameSelection &&
                state1.texts == state2.texts && // Data class list comparison
                state1.stickers == state2.stickers && // Data class list comparison
                state1.stickerBitmapPath == state2.stickerBitmapPath &&
                state1.filter == state2.filter &&
                state1.blur == state2.blur &&
                state1.brightness == state2.brightness &&
                state1.contrast == state2.contrast &&
                state1.saturation == state2.saturation
    }

    /**
     * Lấy state cuối cùng trong redo stack (for checking)
     */
    fun getLastState(): CollageState? = redoStack.lastOrNull()

    /**
     * Undo: lấy state trước state hiện tại từ redo stack và đưa vào undo stack
     * State hiện tại (last) sẽ được đưa vào undo stack
     * State trước đó (second last) sẽ được return để restore
     * @return state trước đó, hoặc null nếu không thể undo
     */
    fun undo(): CollageState? {
        if (!canUndo() || redoStack.size < 2) return null

        // Lấy state hiện tại (last) và đưa vào undo stack
        val currentState = redoStack.removeLast()
        if (undoStack.size >= maxStackSize) {
            undoStack.removeFirst()
        }
        undoStack.addLast(currentState.copy())

        // Lấy state trước đó (state cần restore)
        val previousState = redoStack.lastOrNull()
        return previousState?.copy()
    }

    /**
     * Redo: lấy last state từ undo stack và đưa vào redo stack
     * @return state tiếp theo, hoặc null nếu không thể redo
     */
    fun redo(): CollageState? {
        if (!canRedo()) return null

        // Lấy last state từ undo stack
        val state = undoStack.removeLast()

        // Đưa vào redo stack
        if (redoStack.size >= maxStackSize) {
            redoStack.removeFirst()
        }
        redoStack.addLast(state.copy())

        return state
    }

    /**
     * Kiểm tra có thể undo không (redo stack phải có ít nhất 2 phần tử)
     * Vì cần state trước state hiện tại để undo
     */
    fun canUndo(): Boolean = redoStack.size >= 2

    /**
     * Kiểm tra có thể redo không (undo stack phải có phần tử)
     */
    fun canRedo(): Boolean = undoStack.isNotEmpty()

    /**
     * Xóa toàn bộ stack (khi reset hoặc bắt đầu mới)
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    /**
     * Lấy số lượng state trong undo stack (for debugging)
     */
    fun getUndoCount(): Int = undoStack.size

    /**
     * Lấy số lượng state trong redo stack (for debugging)
     */
    fun getRedoCount(): Int = redoStack.size
}

